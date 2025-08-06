import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { CommonService } from './Common.service';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class MenuService {
  private envUrl = environment.apiUrl;
  private commonService = inject(CommonService);

  getMenu() {
    return this.commonService.get(`${this.envUrl}/menu`);
  }
}
