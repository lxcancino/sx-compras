import { Component, OnInit, Input } from '@angular/core';
import { ReportService } from '../../../reportes/services/report.service';

import { TdDialogService } from '@covalent/core';

@Component({
  selector: 'sx-print-cheque',
  template: `
  <ng-container *ngIf="isVisible()">
    <button mat-button [color]="color" (click)="runReport()" >
      <mat-icon>picture_as_pdf</mat-icon> Cheque
    </button>
  </ng-container>
  `
})
export class PrintChequeComponent implements OnInit {
  @Input()
  color = 'accent';
  @Input()
  title = 'Imprimir';
  @Input()
  smallIcon = false;

  @Input()
  egreso: any;

  constructor(
    private service: ReportService,
    private dialogService: TdDialogService
  ) {}

  ngOnInit() {}

  runReport() {
    this.confirmar();
  }

  private confirmar() {
    this.dialogService
      .openConfirm({
        title: 'Impresión de Cheque',
        message: 'Folio: ' + this.egreso.cheque.folio,
        acceptButton: 'Aceptar',
        cancelButton: 'Cancelar'
      })
      .afterClosed()
      .subscribe(res => {
        const url = `tesoreria/cheques/print/${this.egreso.cheque.id}`;
        this.service.runReport(url, {});
      });
  }

  isVisible() {
    return this.egreso && this.egreso.cheque;
  }
}
