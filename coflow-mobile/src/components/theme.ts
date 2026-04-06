import { EstadoTarea, Prioridad } from '../types/types';

export const COLORS = {
  primary: '#1a73e8',
  deepBlue: '#0d1b2a',
  surface: '#1e2d3d',
  surfaceLight: '#253545',
  text: '#ffffff',
  textMuted: '#8899aa',
  border: '#2e3f50',
  success: '#2ecc71',
  danger: '#e74c3c',
  warning: '#f39c12',
  info: '#3498db',
  secondary: '#6c757d',
};

export const ESTADO_LABEL: Record<EstadoTarea, string> = {
  PENDIENTE: 'Pendiente',
  ASIGNADA: 'Asignada',
  EN_PROCESO: 'En proceso',
  BLOQUEADA: 'Bloqueada',
  EN_REVISION: 'En revisión',
  APROBADA: 'Aprobada',
  RECHAZADA: 'Rechazada',
  COMPLETADA: 'Completada',
};

export const ESTADO_COLOR: Record<EstadoTarea, string> = {
  PENDIENTE: COLORS.secondary,
  ASIGNADA: COLORS.primary,
  EN_PROCESO: COLORS.info,
  BLOQUEADA: COLORS.danger,
  EN_REVISION: COLORS.warning,
  APROBADA: COLORS.success,
  RECHAZADA: COLORS.danger,
  COMPLETADA: COLORS.success,
};

export const PRIORIDAD_COLOR: Record<Prioridad, string> = {
  BAJA: COLORS.success,
  MEDIA: COLORS.warning,
  ALTA: COLORS.danger,
  URGENTE: '#1a1a1a',
};

export function formatDate(d?: string) {
  if (!d) return '—';
  return new Date(d).toLocaleDateString('es-ES', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });
}
