import React, { useState } from "react";
import { proyectoService } from "../../services/proyectoService";
import { ProyectoCreateRequest } from "../../types/types";
import { nowMin, closeBsModal } from "./utils";

interface Props {
    onCreated: () => void;
}

export default function CreateProyectoModal({ onCreated }: Props) {
    const [form, setForm] = useState<Partial<ProyectoCreateRequest>>({});
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!form.titulo?.trim()) { setError("El título es obligatorio"); return; }
        setLoading(true);
        try {
            await proyectoService.crear(form as ProyectoCreateRequest);
            setForm({});
            setError("");
            onCreated();
            closeBsModal("closeProyectoModal");
        } catch (e: any) { setError(e.message); }
        finally { setLoading(false); }
    };

    return (
        <div className="modal fade" id="createProyectoModal" tabIndex={-1}>
            <div className="modal-dialog">
                <div className="modal-content">
                    <div className="modal-header">
                        <h5 className="modal-title">Nuevo proyecto</h5>
                        <button id="closeProyectoModal" type="button" className="btn-close" data-bs-dismiss="modal" />
                    </div>
                    <form onSubmit={handleSubmit}>
                        <div className="modal-body">
                            {error && <div className="alert alert-danger py-2">{error}</div>}
                            <div className="mb-3">
                                <label className="form-label">Título *</label>
                                <input className="form-control" maxLength={200} value={form.titulo ?? ""} onChange={e => setForm({ ...form, titulo: e.target.value })} />
                            </div>
                            <div className="mb-3">
                                <label className="form-label">Descripción</label>
                                <textarea className="form-control" rows={2} value={form.descripcion ?? ""} onChange={e => setForm({ ...form, descripcion: e.target.value })} />
                            </div>
                            <div className="mb-3">
                                <label className="form-label">Fecha fin estimada</label>
                                <input type="datetime-local" className="form-control" min={nowMin()} value={form.fechaFinEstimada ?? ""} onChange={e => setForm({ ...form, fechaFinEstimada: e.target.value })} />
                            </div>
                        </div>
                        <div className="modal-footer">
                            <button type="button" className="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                            <button type="submit" className="btn btn-primary" disabled={loading}>{loading ? "Creando…" : "Crear proyecto"}</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}
