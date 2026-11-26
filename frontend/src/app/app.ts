import { CurrencyPipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Logistics, LogisticsApiService } from './logistics-api.service';

@Component({
  imports: [CurrencyPipe, FormsModule],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App implements OnInit {
  private readonly logisticsApi = inject(LogisticsApiService);
  private readonly changeDetector = inject(ChangeDetectorRef);

  logistics: Logistics[] = [];
  loading = true;
  saving = false;
  editingId: number | null = null;
  errorMessage = '';
  successMessage = '';
  form = this.emptyForm();

  get reservedCount(): number {
    return this.logistics.filter((item) => item.reserve).length;
  }

  get reservedTotal(): number {
    return this.logistics
      .filter((item) => item.reserve)
      .reduce((total, item) => total + item.prixUnit * item.quantite, 0);
  }

  ngOnInit(): void {
    this.loadLogistics();
  }

  loadLogistics(): void {
    this.loading = true;
    this.errorMessage = '';
    this.logisticsApi.getAll().subscribe({
      next: (items) => {
        this.logistics = items;
        this.loading = false;
        this.changeDetector.markForCheck();
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = this.describeError(error);
        this.loading = false;
        this.changeDetector.markForCheck();
      },
    });
  }

  saveLogistics(): void {
    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    const request = this.editingId === null
      ? this.logisticsApi.create(this.form)
      : this.logisticsApi.update(this.editingId, this.form);

    request.subscribe({
      next: () => {
        this.successMessage = this.editingId === null
          ? 'La logistique a été ajoutée.'
          : 'La logistique a été modifiée.';
        this.cancelEdit();
        this.saving = false;
        this.loadLogistics();
        this.changeDetector.markForCheck();
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = this.describeError(error);
        this.saving = false;
        this.changeDetector.markForCheck();
      },
    });
  }

  editLogistics(item: Logistics): void {
    this.editingId = item.idLog;
    this.form = {
      description: item.description,
      reserve: item.reserve,
      prixUnit: item.prixUnit,
      quantite: item.quantite,
    };
    this.successMessage = '';
    this.errorMessage = '';
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  deleteLogistics(item: Logistics): void {
    if (!window.confirm(`Supprimer « ${item.description} » ?`)) {
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';
    this.logisticsApi.delete(item.idLog).subscribe({
      next: () => {
        this.successMessage = 'La logistique a été supprimée.';
        if (this.editingId === item.idLog) {
          this.cancelEdit();
        }
        this.loadLogistics();
        this.changeDetector.markForCheck();
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = this.describeError(error);
        this.changeDetector.markForCheck();
      },
    });
  }

  cancelEdit(): void {
    this.editingId = null;
    this.form = this.emptyForm();
  }

  private emptyForm(): Omit<Logistics, 'idLog'> {
    return {
      description: '',
      reserve: false,
      prixUnit: 0,
      quantite: 1,
    };
  }

  private describeError(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'Impossible de joindre l’API. Vérifie que Spring Boot tourne sur le port 8089.';
    }
    if (error.status === 404) {
      return 'Cette logistique n’existe plus. Actualise la liste.';
    }
    return `Erreur API (${error.status}) : ${error.message}`;
  }
}
