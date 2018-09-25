import { Action } from '@ngrx/store';

import { Requisicion } from '../../model';

export enum RequisicionActionTypes {
  LOAD = '[Requisicion de compra] Load',
  LOAD_FAIL = '[Requisicion de compra] Load Fail',

  // CRUD
  SAVE_REQUISICION = '[Requisicion de compra] Save',
  SAVE_REQUISICION_FAIL = '[Requisicion de compra] Save fail',
  SAVE_REQUISICION_SUCCESS = '[Requisicion de compra] Save success',

  UPDATE_REQUISICION = '[Requisicion de compra] Update',
  UPDATE_REQUISICION_FAIL = '[Requisicion de compra] Update Fail',
  UPDATE_REQUISICION_SUCCESS = '[Requisicion de compra] Update Success',

  DELETE_REQUISICION = '[Requisicion de compra] Delete',
  DELETE_REQUISICION_FAIL = '[Requisicion de compra] Delete Fail',
  DELETE_REQUISICION_SUCCESS = '[Requisicion de compra] Delete Success',

  // Cerrar el analisis
  CERRAR_REQUISICION = '[Requisicion de compra] Cerrar',
  CERRAR_REQUISICION_FAIL = '[Requisicion de compra] Cerrar Fail',
  CERRAR_REQUISICION_SUCCESS = '[Requisicion de compra] Cerrar Success'
}

export class LoadRequisicion implements Action {
  readonly type = RequisicionActionTypes.LOAD;
  constructor(public payload: Requisicion) {}
}
export class LoadRequisicionFail implements Action {
  readonly type = RequisicionActionTypes.LOAD_FAIL;
  constructor(public payload: any) {}
}

// Save
export class SaveRequisicion implements Action {
  readonly type = RequisicionActionTypes.SAVE_REQUISICION;
  constructor(public payload: Requisicion) {}
}
export class SaveRequisicionFail implements Action {
  readonly type = RequisicionActionTypes.SAVE_REQUISICION_FAIL;
  constructor(public payload: any) {}
}
export class SaveRequisicionSuccess implements Action {
  readonly type = RequisicionActionTypes.SAVE_REQUISICION_SUCCESS;
  constructor(public payload: Requisicion) {}
}

// Update
export class UpdateRequisicion implements Action {
  readonly type = RequisicionActionTypes.UPDATE_REQUISICION;
  constructor(public payload: Requisicion) {}
}
export class UpdateRequisicionFail implements Action {
  readonly type = RequisicionActionTypes.UPDATE_REQUISICION_FAIL;
  constructor(public payload: any) {}
}
export class UpdateRequisicionSuccess implements Action {
  readonly type = RequisicionActionTypes.UPDATE_REQUISICION_SUCCESS;
  constructor(public payload: Requisicion) {}
}

// Delete
export class DeleteRequisicion implements Action {
  readonly type = RequisicionActionTypes.DELETE_REQUISICION;
  constructor(public payload: Requisicion) {}
}
export class DeleteRequisicionFail implements Action {
  readonly type = RequisicionActionTypes.DELETE_REQUISICION_FAIL;
  constructor(public payload: any) {}
}
export class DeleteRequisicionSuccess implements Action {
  readonly type = RequisicionActionTypes.DELETE_REQUISICION_SUCCESS;
  constructor(public payload: Requisicion) {}
}

// Cerrar
export class CerrarRequisicion implements Action {
  readonly type = RequisicionActionTypes.CERRAR_REQUISICION;
  constructor(public payload: Requisicion) {}
}
export class CerrarRequisicionFail implements Action {
  readonly type = RequisicionActionTypes.CERRAR_REQUISICION_FAIL;
  constructor(public payload: any) {}
}
export class CerrarRequisicionSuccess implements Action {
  readonly type = RequisicionActionTypes.CERRAR_REQUISICION_SUCCESS;
  constructor(public payload: Requisicion) {}
}

export type RequisicionActions =
  | LoadRequisicion
  | LoadRequisicionFail
  | SaveRequisicion
  | SaveRequisicionFail
  | SaveRequisicionSuccess
  | UpdateRequisicion
  | UpdateRequisicionFail
  | UpdateRequisicionSuccess
  | DeleteRequisicion
  | DeleteRequisicionFail
  | DeleteRequisicionSuccess
  | CerrarRequisicion
  | CerrarRequisicionFail
  | CerrarRequisicionSuccess;
