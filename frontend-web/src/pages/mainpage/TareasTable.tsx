import React from "react";
import { useAuth } from "../../auth/AuthProvider";
import { tareaService } from "../../services/tareaService";
import { Tarea, EstadoTarea } from "../../types/types";
import { ESTADO_LABEL, ESTADO_BADGE, PRIORIDAD_BADGE } from "./constants";
import { formatDate } from "./utils";

// ─── TareaRow ─────────────────────────────────────────────────────────────────

interface TareaRowProps {
    tarea: Tarea;
    onRefresh: () => void;
    onVerComentarios: (t: Tarea) => void;
}

function TareaRow({ tarea, onRefresh, onVerComentarios }: TareaRowProps) {
    const { user } = useAuth();
    const esAsignado = tarea.usuarioAsignado?.id === user?.usuarioId;

    const cambiarEstado = async (accion: "ACEPTAR" | "RECHAZAR") => {
        try { await tareaService.cambiarEstado(tarea.id, { accion }); onRefresh(); }
        catch (e: any) { alert(e.message); }
    };

    const mover = async (estadoDestino: EstadoTarea) => {
        try { await tareaService.moverEstado(tarea.id, { estadoDestino }); onRefresh(); }
        catch (e: any) { alert(e.message); }
    };

    return (
        <tr>
            <td className="fw-semibold">{tarea.titulo}</td>
            <td><span className={`badge bg-${ESTADO_BADGE[tarea.estado]}`}>{ESTADO_LABEL[tarea.estado]}</span></td>
            <td><span className={`badge bg-${PRIORIDAD_BADGE[tarea.prioridad]}`}>{tarea.prioridad}</span></td>
            <td className="text-muted small">{tarea.proyectoTitulo}</td>
            <td className="text-muted small">{tarea.usuarioAsignado?.nombreCompleto ?? "—"}</td>
            <td className="text-muted small">{formatDate(tarea.fechaLimite)}</td>
            <td>
                <div className="d-flex gap-1 flex-wrap">
                    {esAsignado && tarea.estado === "ASIGNADA" && (<>
                        <button className="btn btn-sm btn-success" onClick={() => cambiarEstado("ACEPTAR")}>Aceptar</button>
                        <button className="btn btn-sm btn-outline-danger" onClick={() => cambiarEstado("RECHAZAR")}>Rechazar</button>
                    </>)}
                    {esAsignado && tarea.estado === "EN_PROCESO" && (
                        <button className="btn btn-sm btn-primary" onClick={() => mover("COMPLETADA")}>
                            Completar
                        </button>
                    )}
                    <button
                        className="btn btn-sm btn-outline-secondary"
                        title="Ver comentarios"
                        onClick={() => onVerComentarios(tarea)}
                    >
                        {tarea.totalComentarios > 0 && <span className="ms-1 badge bg-secondary">{tarea.totalComentarios}</span>}
                    </button>
                </div>
            </td>
        </tr>
    );
}

// ─── TareasTable ──────────────────────────────────────────────────────────────

interface TareasTableProps {
    tareas: Tarea[];
    onRefresh: () => void;
    onVerComentarios: (t: Tarea) => void;
}

export default function TareasTable({ tareas, onRefresh, onVerComentarios }: TareasTableProps) {
    if (tareas.length === 0) return <p className="text-muted">No hay tareas para mostrar.</p>;
    return (
        <div className="table-responsive">
            <table className="table table-hover align-middle">
                <thead className="table-light">
                    <tr>
                        <th>Título</th><th>Estado</th><th>Prioridad</th>
                        <th>Proyecto</th><th>Asignado a</th><th>Fecha límite</th><th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    {tareas.map(t => <TareaRow key={t.id} tarea={t} onRefresh={onRefresh} onVerComentarios={onVerComentarios} />)}
                </tbody>
            </table>
        </div>
    );
}
