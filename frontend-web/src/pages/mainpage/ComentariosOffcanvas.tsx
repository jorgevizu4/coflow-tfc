import React, { useCallback, useEffect, useRef, useState } from "react";
import { useAuth } from "../../auth/AuthProvider";
import { comentarioService } from "../../services/comentarioService";
import { Tarea, Comentario } from "../../types/types";

interface Props {
    tarea: Tarea | null;
    onClose: () => void;
}

export default function ComentariosOffcanvas({ tarea, onClose }: Props) {
    const { user } = useAuth();
    const [comentarios, setComentarios] = useState<Comentario[]>([]);
    const [texto, setTexto] = useState("");
    const [loading, setLoading] = useState(false);
    const [sending, setSending] = useState(false);
    const endRef = useRef<HTMLDivElement>(null);

    const cargar = useCallback(async () => {
        if (!tarea) return;
        setLoading(true);
        try {
            const res = await comentarioService.listarPorTarea(tarea.id);
            setComentarios(res.data);
        } catch { /* ignore */ }
        finally { setLoading(false); }
    }, [tarea]);

    useEffect(() => { cargar(); setTexto(""); }, [cargar]);
    useEffect(() => { endRef.current?.scrollIntoView({ behavior: "smooth" }); }, [comentarios]);

    const enviar = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!texto.trim() || !tarea) return;
        setSending(true);
        try {
            await comentarioService.crear({ tareaId: tarea.id, contenido: texto.trim() });
            setTexto("");
            await cargar();
        } catch { /* ignore */ }
        finally { setSending(false); }
    };

    const eliminar = async (id: number) => {
        try { await comentarioService.eliminar(id); await cargar(); }
        catch { /* ignore */ }
    };

    const formatTs = (d: string) =>
        new Date(d).toLocaleString("es-ES", { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" });

    return (
        <div
            className={`offcanvas offcanvas-end${tarea ? " show" : ""}`}
            style={{ width: 380, visibility: tarea ? "visible" : "hidden" }}
            tabIndex={-1}
        >
            <div className="offcanvas-header" style={{ background: "var(--deep-space-blue)" }}>
                <div>
                    <h6 className="offcanvas-title text-white mb-0">Comentarios</h6>
                    {tarea && <small className="text-white-50">{tarea.titulo}</small>}
                </div>
                <button type="button" className="btn-close btn-close-white" onClick={onClose} />
            </div>
            <div className="offcanvas-body d-flex flex-column p-0">
                <div className="flex-grow-1 overflow-auto p-3">
                    {loading && <div className="text-center text-muted py-4"><div className="spinner-border spinner-border-sm" /></div>}
                    {!loading && comentarios.length === 0 && (
                        <p className="text-muted text-center small mt-4">Sin comentarios todavía. ¡Sé el primero!</p>
                    )}
                    {comentarios.map(c => (
                        <div key={c.id} className="card mb-2 border-0 shadow-sm">
                            <div className="card-body py-2 px-3">
                                <div className="d-flex justify-content-between align-items-start">
                                    <span className="fw-semibold small">{c.autorNombre}</span>
                                    {c.autorId === user?.usuarioId && (
                                        <button className="btn btn-link btn-sm p-0 text-danger ms-2" title="Eliminar" onClick={() => eliminar(c.id)}>
                                            <i className="bi bi-trash" />
                                        </button>
                                    )}
                                </div>
                                <p className="mb-1 small">{c.contenido}</p>
                                <small className="text-muted">{formatTs(c.fechaCreacion)}</small>
                            </div>
                        </div>
                    ))}
                    <div ref={endRef} />
                </div>
                <form onSubmit={enviar} className="border-top p-3 d-flex gap-2">
                    <input
                        className="form-control form-control-sm"
                        placeholder="Escribe un comentario…"
                        value={texto}
                        onChange={e => setTexto(e.target.value)}
                        maxLength={500}
                        disabled={sending}
                    />
                    <button type="submit" className="btn btn-primary btn-sm" disabled={sending || !texto.trim()}>
                        {sending ? <span className="spinner-border spinner-border-sm" /> : "Enviar"} <i className="bi bi-send" />
                    </button>
                </form>
            </div>
        </div>
    );
}
