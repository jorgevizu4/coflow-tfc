import React, { useState } from "react";
import { tareaService } from "../../services/tareaService";
import { proyectoService } from "../../services/proyectoService";
import { Proyecto, Prioridad, TareaCreateRequest, UsuarioResumido } from "../../types/types";
import { nowMin, closeBsModal } from "./utils";

interface Props {
    proyectos: Proyecto[];
    onCreated: () => void;
}

export default function CreateTareaModal({ proyectos, onCreated }: Props) {
    const [form, setForm] = useState<Partial<TareaCreateRequest>>({ prioridad: "MEDIA" });
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const [miembros, setMiembros] = useState<UsuarioResumido[]>([]);

    const handleProyectoChange = async (proyectoId: number) => {
        setForm(f => ({ ...f, proyectoId, usuarioAsignadoId: undefined }));
        setMiembros([]);
        if (!proyectoId) return;
        try {
            const res = await proyectoService.getMiembros(proyectoId);
            setMiembros(res.data);
        } catch (e: any) {
            setError(e.message ?? "Error al cargar miembros");
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!form.proyectoId || !form.titulo?.trim()) { setError("Proyecto y título son obligatorios"); return; }
        if (!form.usuarioAsignadoId) { setError("Debes asignar la tarea a un miembro del equipo"); return; }
        setLoading(true);
        try {
            await tareaService.crear(form as TareaCreateRequest);
            setForm({ prioridad: "MEDIA" });
            setError("");
            onCreated();
            closeBsModal("closeTareaModal");
        } catch (e: any) { setError(e.message); }
        finally { setLoading(false); }
    };

    return (
        <div className="modal fade" id="createTareaModal" tabIndex={-1}>
            <div className="modal-dialog">
                <div className="modal-content">
                    <div className="modal-header">
                        <h5 className="modal-title">Nueva tarea</h5>
                        <button id="closeTareaModal" type="button" className="btn-close" data-bs-dismiss="modal" />
                    </div>
                    <form onSubmit={handleSubmit}>
                        <div className="modal-body">
                            {error && <div className="alert alert-danger py-2">{error}</div>}
                            <div className="mb-3">
                                <label className="form-label">Proyecto *</label>
                                <select className="form-select" value={form.proyectoId ?? ""} onChange={e => handleProyectoChange(Number(e.target.value))}>
                                    <option value="">Selecciona un proyecto</option>
                                    {proyectos.map(p => <option key={p.id} value={p.id}>{p.titulo}</option>)}
                                </select>
                            </div>
                            <div className="mb-3">
                                <label className="form-label">Asignar a</label>
                                <select
                                    className="form-select"
                                    value={form.usuarioAsignadoId ?? ""}
                                    onChange={e => setForm({ ...form, usuarioAsignadoId: e.target.value ? Number(e.target.value) : undefined })}
                                    disabled={miembros.length === 0}
                                >
                                    <option value="">{form.proyectoId ? (miembros.length === 0 ? "Cargando…" : "Sin asignar") : "Selecciona un proyecto primero"}</option>
                                    {miembros.map(m => <option key={m.id} value={m.id}>{m.nombreCompleto} — {m.email}</option>)}
                                </select>
                            </div>
                            <div className="mb-3">
                                <label className="form-label">Título *</label>
                                <input className="form-control" maxLength={200} value={form.titulo ?? ""} onChange={e => setForm({ ...form, titulo: e.target.value })} />
                            </div>
                            <div className="mb-3">
                                <label className="form-label">Descripción</label>
                                <textarea className="form-control" rows={2} value={form.descripcion ?? ""} onChange={e => setForm({ ...form, descripcion: e.target.value })} />
                            </div>
                            <div className="row g-2 mb-3">
                                <div className="col">
                                    <label className="form-label">Prioridad</label>
                                    <select className="form-select" value={form.prioridad} onChange={e => setForm({ ...form, prioridad: e.target.value as Prioridad })}>
                                        <option value="BAJA">Baja</option>
                                        <option value="MEDIA">Media</option>
                                        <option value="ALTA">Alta</option>
                                        <option value="URGENTE">Urgente</option>
                                    </select>
                                </div>
                                <div className="col">
                                    <label className="form-label">Fecha límite</label>
                                    <input type="datetime-local" className="form-control" min={nowMin()} value={form.fechaLimite ?? ""} onChange={e => setForm({ ...form, fechaLimite: e.target.value })} />
                                </div>
                            </div>
                        </div>
                        <div className="modal-footer">
                            <button type="button" className="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                            <button type="submit" className="btn btn-primary" disabled={loading}>{loading ? "Creando…" : "Crear tarea"}</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}
