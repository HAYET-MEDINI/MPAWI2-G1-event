import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface Logistics {
  idLog: number;
  description: string;
  reserve: boolean;
  prixUnit: number;
  quantite: number;
}

@Injectable({ providedIn: 'root' })
export class LogisticsApiService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = '/events/logistics';

  getAll(): Observable<Logistics[]> {
    return this.http.get<Logistics[]>(this.endpoint);
  }

  create(item: Omit<Logistics, 'idLog'>): Observable<Logistics> {
    return this.http.post<Logistics>(this.endpoint, item);
  }

  update(id: number, item: Omit<Logistics, 'idLog'>): Observable<Logistics> {
    return this.http.put<Logistics>(`${this.endpoint}/${id}`, item);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.endpoint}/${id}`);
  }
}
