import React, { useCallback, useEffect, useState } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { library } from "@fortawesome/fontawesome-svg-core";
import { fas } from "@fortawesome/free-solid-svg-icons";
import DefaultLayout from "../components/DefaultLayout";
import { useAuth } from "../auth/AuthProvider";
import { tareaService } from "../services/tareaService";
import { proyectoService } from "../services/proyectoService";
import { Tarea, Proyecto, EstadoTarea } from "../types/types";
import { formatDate } from "./mainpage/utils";
import ComentariosOffcanvas from "./mainpage/ComentariosOffcanvas";
import TareasTable from "./mainpage/TareasTable";
import CreateTareaModal from "./mainpage/CreateTareaModal";
import CreateProyectoModal from "./mainpage/CreateProyectoModal";

library.add(fas);

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

    const statCards: { label: string; estado: EstadoTarea; color: string }[] = [
        { label: "Pendientes",  estado: "PENDIENTE",  color: "secondary" },
        { label: "En proceso",  estado: "EN_PROCESO", color: "info"      },
        { label: "Rechazadas",  estado: "RECHAZADA",  color: "danger"    },
        { label: "Completadas", estado: "COMPLETADA", color: "success"   },
    ];

    return (
        <DefaultLayout>
            <div className="d-flex" style={{ minHeight: "calc(100vh - 116px)" }}>

                {/* ── Sidebar ── */}
                <nav className="d-flex flex-column p-3 border-end border-top flex-shrink-0"
                    style={{ width: 220, background: "var(--deep-space-blue)" }}>

                    <p className="text-white-50 small fw-bold text-uppercase mb-2 mt-1 ps-1">Navegación</p>

                    {(["dashboard", "mis-tareas", "todas", "proyectos"] as const).map(s => {
                        const labels: Record<string, React.ReactNode> = {
                            "dashboard":  <><FontAwesomeIcon icon={['fas', 'chart-line']} className="me-2" />Dashboard</>,
                            "mis-tareas": <><FontAwesomeIcon icon={['fas', 'clipboard']} className="me-2" />Mis tareas</>,
                            "todas":      <><FontAwesomeIcon icon={['fas', 'folder']} className="me-2" />Todas las tareas</>,
                            "proyectos":  <><FontAwesomeIcon icon={['fas', 'diagram-project']} className="me-2" />Proyectos</>,
                        };
                        const isActive = section === s;
                        return (
                            <button key={s} onClick={() => setSection(s)}
                                className="btn btn-sm text-start mb-1 w-100 text-white border-0"
                                style={{
                                    background: isActive ? "rgba(255,255,255,0.15)" : "transparent",
                                    borderLeft: isActive ? "3px solid #fff" : "3px solid transparent",
                                    borderRadius: 6,
                                    padding: "8px 12px",
                                    fontWeight: isActive ? 600 : 400,
                                    fontSize: "0.875rem",
                                    letterSpacing: "0.01em",
                                    transition: "background 0.15s",
                                }}>
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
                                        <div className={`card bg-${c.color} border-0 h-100 shadow-sm`}>
                                            <div className="card-body text-center py-3">
                                                <h2 className="text-white mb-1 fw-bold">{stats[c.estado] ?? 0}</h2>
                                                <p className="text-white mb-0 fw-semibold" style={{ fontSize: "0.85rem", letterSpacing: "0.04em", textTransform: "uppercase" }}>{c.label}</p>
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