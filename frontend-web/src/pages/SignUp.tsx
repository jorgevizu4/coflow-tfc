import React, { useEffect, useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";    
import { useAuth } from "../auth/AuthProvider";
import { empresaService, EmpresaResumida } from "../services/empresaService";

export default function Signup() {

    const [empresas, setEmpresas] = useState<EmpresaResumida[]>([]);
    const [input, setInput] = useState({
        empresaId: "" as string | number,
        nombre: "",
        apellidos: "",
        email: "",
        password: "",
        passwordRepeat: ""
    });

    const [errorResponse, setErrorResponse] = useState("");
    const [loading, setLoading] = useState(false);
    const passwordsStarted = input.password.length > 0 || input.passwordRepeat.length > 0;
    const passwordsMatch = input.password === input.passwordRepeat;
    const hasPasswordMismatch = passwordsStarted && !passwordsMatch;

    const auth = useAuth();
    const goTo = useNavigate();

    useEffect(() => {
        empresaService.listar()
            .then(setEmpresas)
            .catch(() => setErrorResponse("No se pudieron cargar las empresas. ¿Está el servidor activo?"));
    }, []);

    async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        if (
            !input.empresaId ||
            !input.nombre.trim() ||
            !input.email.trim() ||
            !input.password.trim() ||
            !input.passwordRepeat.trim()
        ) {
            setErrorResponse("Debes rellenar todos los campos obligatorios.");
            return;
        }

        if (hasPasswordMismatch) {
            setErrorResponse("Las contraseñas no coinciden");
            return;
        }
        setLoading(true);
        try {
            await auth.signup(
                Number(input.empresaId),
                input.nombre,
                input.apellidos,
                input.email,
                input.password,
                input.passwordRepeat
            );
            setErrorResponse("");
            goTo("/main");
        } catch (error: any) {
            setErrorResponse(error.message || "Error al registrarse");
        } finally {
            setLoading(false);
        }
    }

    if (auth.isAuthenticated) {
        return <Navigate to="/main" />;
    }

    return (
        <div className="auth-page d-flex flex-column justify-content-center align-items-center bg-light">
            <div className="auth-card p-4 shadow rounded bg-white border">
                <div className="mb-4 text-center border-bottom pb-3">
                    <h1>CoFlow</h1>
                    <h4>Gestor de proyectos colaborativo</h4>
                    {!!errorResponse && <div className="errorMessage alert alert-danger mt-3">{errorResponse}</div>}
                </div>
                <form className="login-form" onSubmit={handleSubmit}>
                    <div className="mb-3">
                        <label htmlFor="empresa" className="form-label">Empresa *</label>
                        <select className="form-select" id="empresa" value={input.empresaId}
                            onChange={e => setInput({ ...input, empresaId: e.target.value })}>
                            <option value="">Selecciona tu empresa</option>
                            {empresas.map(emp => (
                                <option key={emp.id} value={emp.id}>{emp.nombre}</option>
                            ))}
                        </select>
                    </div>
                    <div className="row g-2 mb-3">
                        <div className="col">
                            <label htmlFor="nombre" className="form-label">Nombre *</label>
                            <input type="text" className="form-control" id="nombre" placeholder="Nombre"
                                value={input.nombre}
                                onChange={e => setInput({ ...input, nombre: e.target.value })} />
                        </div>
                        <div className="col">
                            <label htmlFor="apellidos" className="form-label">Apellidos</label>
                            <input type="text" className="form-control" id="apellidos" placeholder="Apellidos"
                                value={input.apellidos}
                                onChange={e => setInput({ ...input, apellidos: e.target.value })} />
                        </div>
                    </div>
                    <div className="mb-3">
                        <label htmlFor="email" className="form-label">Email *</label>
                        <input type="email" className="form-control" id="email" placeholder="tu@email.com"
                            value={input.email}
                            onChange={e => setInput({ ...input, email: e.target.value })} />
                    </div>
                    <div className="mb-3">
                        <label htmlFor="password" className="form-label">Contraseña *</label>
                        <input type="password" className="form-control" id="password" placeholder="Contraseña"
                            value={input.password}
                            onChange={e => setInput({ ...input, password: e.target.value })} />
                    </div>
                    <div className="mb-3">
                        <label htmlFor="passwordRepeat" className="form-label">Repetir contraseña *</label>
                        <input type="password"
                            className={`form-control ${hasPasswordMismatch ? "is-invalid" : passwordsStarted ? "is-valid" : ""}`}
                            id="passwordRepeat" placeholder="Repite la contraseña"
                            value={input.passwordRepeat}
                            onChange={e => setInput({ ...input, passwordRepeat: e.target.value })} />
                        {hasPasswordMismatch && (
                            <div className="invalid-feedback d-block">Las contraseñas no coinciden.</div>
                        )}
                        {!hasPasswordMismatch && passwordsStarted && (
                            <div className="text-success small mt-1">Las contraseñas coinciden.</div>
                        )}
                    </div>
                    <button type="submit" className="btn btn-primary w-100 mb-3" disabled={loading || hasPasswordMismatch}>
                        {loading ? "Cargando..." : "Registrarse"}
                    </button>
                    <button type="button" className="btn btn-outline-secondary w-100" onClick={() => goTo("/login")} disabled={loading}>
                        Volver al inicio de sesión
                    </button>
                </form>
            </div>
        </div>
    );
}