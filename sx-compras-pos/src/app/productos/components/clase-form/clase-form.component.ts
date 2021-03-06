import {
  Component,
  OnInit,
  Inject,
  ChangeDetectionStrategy
} from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

import { MAT_DIALOG_DATA } from '@angular/material';
import { Clase } from '../../models/clase';

@Component({
  selector: 'sx-clase-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './clase-form.component.html'
})
export class ClaseFormComponent implements OnInit {
  form: FormGroup;
  clase: Clase;
  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: FormBuilder
  ) {
    this.clase = data.clase;
  }

  ngOnInit() {
    this.form = this.fb.group({
      clase: [
        this.clase ? this.clase.clase : null,
        [Validators.required, Validators.minLength(5), Validators.maxLength(50)]
      ],
      activa: [this.clase ? this.clase.activa : true]
    });
  }

  get control() {
    return this.form.get('clase');
  }

  private hasError(code: string) {
    return this.control.hasError(code);
  }

  getError() {
    if (this.hasError('minlength')) {
      const error = this.control.getError('minlength');
      return `Longitud mínima requerida ${error.requiredLength}`;
    } else if (this.hasError('maxlength')) {
      const error = this.control.getError('maxlength');
      return `Longitud máxima  (${error.requiredLength}) excedida`;
    } else if (this.hasError('required')) {
      return 'Debe digitar un nombre válido';
    }
    return null;
  }
}
