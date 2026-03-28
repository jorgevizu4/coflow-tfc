import React, { useCallback, useEffect, useRef, useState } from "react";
import DefaultLayout from "../components/DefaultLayout";
import { useAuth } from "../auth/AuthProvider";
import { tareaService } from "../services/tareaService";
import { proyectoService } from "../services/proyectoService";
import { comentarioService } from "../services/comentarioService";
import {
    Tarea,
    Proyecto,
    EstadoTarea,
    Prioridad,
    TareaCreateRequest,
    ProyectoCreateRequest,
    UsuarioResumido,
    Comentario,
} from "../types/types";

// ─── Constants ────────────────────────────────────────────────────────────────

const ESTADO_LABEL: Record<EstadoTarea, string> = {
    PENDIENTE:   "Pendiente",
    ASIGNADA:    "Asignada",
    EN_PROCESO:  "En proceso",
    BLOQUEADA:   "Bloqueada",
    EN_REVISION: "En revisión",
    APROBADA:    "Aprobada",
    RECHAZADA:   "Rechazada",
    COMPLETADA:  "Completada",
};

const ESTADO_BADGE: Record<EstadoTarea, string> = {
    PENDIENTE:   "secondary",
    ASIGNADA:    "primary",
    EN_PROCESO:  "info text-dark",
    BLOQUEADA:   "danger",
    EN_REVISION: "warning text-dark",
    APROBADA:    "success",
    RECHAZADA:   "danger",
    COMPLETADA:  "success",
};

const PRIORIDAD_BADGE: Record<Prioridad, string> = {
    BAJA:    "success",
    MEDIA:   "warning text-dark",
    ALTA:    "danger",
    URGENTE: "dark",
};

function formatDate(d?: string) {
    if (!d) return "—";
    return new Date(d).toLocaleDateString("es-ES", { day: "2-digit", month: "short", year: "numeric" });
}

function nowMin() {
    const now = new Date();
    now.setSeconds(0, 0);
    return now.toISOString().slice(0, 16);
}

function closeBsModal(id: string) {
    (document.getElementById(id) as HTMLButtonElement | null)?.click();
}

// ─── ComentariosOffcanvas ─────────────────────────────────────────────────────

function ComentariosOffcanvas({ tarea, onClose }: { tarea: Tarea | null; onClose: () => void }) {
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

// ─── TareaRow ─────────────────────────────────────────────────────────────────

function TareaRow({ tarea, onRefresh, onVerComentarios }: { tarea: Tarea; onRefresh: () => void; onVerComentarios: (t: Tarea) => void }) {
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
                        💬{tarea.totalComentarios > 0 && <span className="ms-1 badge bg-secondary">{tarea.totalComentarios}</span>}
                    </button>
                </div>
            </td>
        </tr>
    );
}

// ─── TareasTable ──────────────────────────────────────────────────────────────

function TareasTable({ tareas, onRefresh, onVerComentarios }: { tareas: Tarea[]; onRefresh: () => void; onVerComentarios: (t: Tarea) => void }) {
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

// ─── CreateTareaModal ─────────────────────────────────────────────────────────

function CreateTareaModal({ proyectos, onCreated }: { proyectos: Proyecto[]; onCreated: () => void }) {
    const [form, setForm] = useState<Partial<TareaCreateRequest>>({ prioridad: "MEDIA" });
    const [error, setError]   = useState("");
    const [loading, setLoading] = useState(false);
    const [miembros, setMiembros] = useState<UsuarioResumido[]>([]);

    const handleProyectoChange = async (proyectoId: number) => {
        setForm(f => ({ ...f, proyectoId, usuarioAsignadoId: undefined }));
        setMiembros([]);
        if (!proyectoId) return;
        try {
            const res = await proyectoService.getMiembros(proyectoId);
            setMiembros(res.data);
        } catch { /* ignore */ }
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

// ─── CreateProyectoModal ──────────────────────────────────────────────────────

function CreateProyectoModal({ onCreated }: { onCreated: () => void }) {
    const [form, setForm] = useState<Partial<ProyectoCreateRequest>>({});
    const [error, setError]   = useState("");
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

// ─── MainPage ─────────────────────────────────────────────────────────────────

type Section = "dashboard" | "mis-tareas" | "todas" | "proyectos";

export default function MainPage() {
    const { user } = useAuth();
    const [section, setSection]   = useState<Section>("dashboard");
    const [tareas, setTareas]     = useState<Tarea[]>([]);
    const [misTareas, setMisTareas] = useState<Tarea[]>([]);
    const [proyectos, setProyectos] = useState<Proyecto[]>([]);
    const [stats, setStats]       = useState<Partial<Record<EstadoTarea, number>>>({});
    const [loading, setLoading]   = useState(false);
    const [error, setError]       = useState("");
    const [tareaSeleccionada, setTareaSeleccionada] = useState<Tarea | null>(null);

    const puedeCrearProy = user?.rol === "ADMIN" || user?.rol === "LIDER";

    const loadData = useCallback(async () => {
        setLoading(true);
        setError("");
        try {
            const [tareasRes, misRes, proyRes] = await Promise.all([
                tareaService.listar(),
                tareaService.misTareas(),
                proyectoService.listar(),
            ]);
            setTareas(tareasRes.data);
            setMisTareas(misRes.data);
            setProyectos(proyRes.data);

            const s: Partial<Record<EstadoTarea, number>> = {};
            tareasRes.data.forEach(t => { s[t.estado] = (s[t.estado] ?? 0) + 1; });
            setStats(s);
        } catch (e: any) {
            setError(e.message ?? "Error al cargar datos");
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => { loadData(); }, [loadData]);

    const statCards: { label: string; estado: EstadoTarea; icon: string; color: string }[] = [
        { label: "Pendientes",   estado: "PENDIENTE",   icon: "⏳", color: "secondary" },
        { label: "En proceso",   estado: "EN_PROCESO",  icon: "🔨", color: "info"      },
        { label: "Rechazadas",   estado: "RECHAZADA",   icon: "⛔", color: "danger"    },
        { label: "Completadas",  estado: "COMPLETADA",  icon: "✅", color: "success"   }
    ];

    return (
        <DefaultLayout>
            <div className="d-flex" style={{ minHeight: "calc(100vh - 116px)" }}>

                {/* ── Sidebar ── */}
                <nav className="d-flex flex-column p-3 border-end border-top flex-shrink-0"
                    style={{ width: 220, background: "var(--deep-space-blue)" }}>

                    <p className="text-white-50 small fw-bold text-uppercase mb-2 mt-1 ps-1">Navegación</p>

                    {(["dashboard", "mis-tareas", "todas", "proyectos"] as const).map(s => {
                        const labels: Record<string, string> = {
                            "dashboard":  "📊 Dashboard",
                            "mis-tareas": "📋 Mis tareas",
                            "todas":      "📁 Todas las tareas",
                            "proyectos":  "🗂️ Proyectos",
                        };
                        return (
                            <button key={s} onClick={() => setSection(s)}
                                className={`btn btn-sm text-start mb-1 ${section === s ? "btn-light fw-semibold" : "btn-outline-light"}`}>
                                {labels[s]}
                            </button>
                        );
                    })}


                    <hr className="border-secondary my-2" />

                    <button className="btn btn-sm btn-outline-light text-start mb-1"
                        data-bs-toggle="modal" data-bs-target="#createTareaModal">
                        ＋ Nueva tarea
                    </button>
                    {puedeCrearProy && (
                        <button className="btn btn-sm btn-outline-light text-start"
                            data-bs-toggle="modal" data-bs-target="#createProyectoModal">
                            ＋ Nuevo proyecto
                        </button>
                    )}

                    <div className="mt-auto pt-3">
                        <p className="text-white small mb-0 text-truncate">{user?.nombreCompleto}</p>
                        <p className="text-white-50 small mb-0">{user?.rol}</p>
                        <p className="text-white-50 small">{user?.empresaNombre}</p>
                    </div>
                </nav>

                {/* ── Content ── */}
                <main className="flex-grow-1 p-4 overflow-auto bg-light">

                    {loading && (
                        <div className="d-flex justify-content-center py-5">
                            <div className="spinner-border text-primary" role="status" />
                        </div>
                    )}

                    {error && !loading && (
                        <div className="alert alert-danger d-flex align-items-center gap-2">
                            {error}
                            <button className="btn btn-sm btn-outline-danger ms-auto" onClick={loadData}>Reintentar</button>
                        </div>
                    )}

                    {/* Dashboard */}
                    {!loading && section === "dashboard" && (
                        <>
                            <h4 className="mb-4 fw-bold">Dashboard</h4>

                            <div className="row g-3 mb-4">
                                {statCards.map(c => (
                                    <div key={c.estado} className="col-6 col-xl-3">
                                        <div className={`card border-${c.color} h-100 shadow-sm`}>
                                            <div className="card-body text-center py-3">
                                                <div style={{ fontSize: 26 }}>{c.icon}</div>
                                                <h2 className={`text-${c.color} mb-0 fw-bold`}>{stats[c.estado] ?? 0}</h2>
                                                <p className="text-muted small mb-0">{c.label}</p>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>

                            <div className="d-flex align-items-center justify-content-between mb-3">
                                <h6 className="mb-0 fw-semibold">Proyectos activos</h6>
                                {puedeCrearProy && (
                                    <button className="btn btn-sm btn-outline-primary"
                                        data-bs-toggle="modal" data-bs-target="#createProyectoModal">
                                        ＋ Nuevo proyecto
                                    </button>
                                )}
                            </div>

                            {proyectos.length === 0 ? (
                                <p className="text-muted">No hay proyectos aún.</p>
                            ) : (
                                <div className="row g-3">
                                    {proyectos.map(p => (
                                        <div key={p.id} className="col-md-6 col-xl-4">
                                            <div className="card h-100 shadow-sm">
                                                <div className="card-body">
                                                    <h6 className="card-title fw-semibold">{p.titulo}</h6>
                                                    {p.descripcion && <p className="card-text text-muted small">{p.descripcion}</p>}
                                                    <p className="small text-muted mb-1">Líder: {p.liderNombre ?? "—"}</p>
                                                    {p.fechaFinEstimada && (
                                                        <p className="small text-muted mb-0">Fin estimado: {formatDate(p.fechaFinEstimada)}</p>
                                                    )}
                                                </div>
                                                <div className="card-footer bg-transparent">
                                                    <button className="btn btn-sm btn-outline-primary w-100"
                                                        onClick={() => setSection("todas")}>
                                                        Ver tareas
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </>
                    )}

                    {/* Mis tareas */}
                    {!loading && section === "mis-tareas" && (
                        <>
                            <h4 className="mb-4 fw-bold">
                                Mis tareas <span className="badge bg-secondary fs-6">{misTareas.length}</span>
                            </h4>
                            <TareasTable tareas={misTareas} onRefresh={loadData} onVerComentarios={setTareaSeleccionada} />
                        </>
                    )}

                    {/* Todas las tareas */}
                    {!loading && section === "todas" && (
                        <>
                            <h4 className="mb-4 fw-bold">
                                Todas las tareas <span className="badge bg-secondary fs-6">{tareas.length}</span>
                            </h4>
                            <TareasTable tareas={tareas} onRefresh={loadData} onVerComentarios={setTareaSeleccionada} />
                        </>
                    )}

                    {/* Proyectos */}
                    {!loading && section === "proyectos" && (
                        <>
                            <div className="d-flex align-items-center justify-content-between mb-4">
                                <h4 className="mb-0 fw-bold">
                                    Proyectos <span className="badge bg-secondary fs-6">{proyectos.length}</span>
                                </h4>
                                {puedeCrearProy && (
                                    <button className="btn btn-primary btn-sm"
                                        data-bs-toggle="modal" data-bs-target="#createProyectoModal">
                                        ＋ Nuevo proyecto
                                    </button>
                                )}
                            </div>
                            {proyectos.length === 0 ? (
                                <p className="text-muted">No hay proyectos todavía.</p>
                            ) : (
                                <div className="row g-3">
                                    {proyectos.map(p => (
                                        <div key={p.id} className="col-md-6 col-xl-4">
                                            <div className="card h-100 shadow-sm">
                                                <div className="card-body">
                                                    <h6 className="card-title fw-semibold">{p.titulo}</h6>
                                                    {p.descripcion && <p className="card-text text-muted small">{p.descripcion}</p>}
                                                    <p className="small text-muted mb-1">Empresa: {p.empresaNombre}</p>
                                                    <p className="small text-muted mb-1">Líder: {p.liderNombre ?? "—"}</p>
                                                    {p.fechaFinEstimada && (
                                                        <p className="small text-muted mb-0">Fin estimado: {formatDate(p.fechaFinEstimada)}</p>
                                                    )}
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </>
                    )}
                </main>
            </div>

            {/* Modals */}
            <CreateTareaModal proyectos={proyectos} onCreated={loadData} />
            {puedeCrearProy && <CreateProyectoModal onCreated={loadData} />}

            {/* Offcanvas backdrop */}
            {tareaSeleccionada && (
                <div className="offcanvas-backdrop fade show" onClick={() => setTareaSeleccionada(null)} />
            )}
            <ComentariosOffcanvas tarea={tareaSeleccionada} onClose={() => setTareaSeleccionada(null)} />
        </DefaultLayout>
    );
}