import React, { useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";    
import { useAuth } from "../auth/AuthProvider";

export default function Signup() {

    const [input, setInput] = useState({
        nombreEmpresa: "",
        nombreAdministrador: "",
        emailAdministrador: "",
        passwordAdministrador: "",
        passwordRepeat: ""
    })

    const [errorResponse, setErrorResponse] = useState("");
    const [loading, setLoading] = useState(false);
    const passwordsStarted = input.passwordAdministrador.length > 0 || input.passwordRepeat.length > 0;
    const passwordsMatch = input.passwordAdministrador === input.passwordRepeat;
    const hasPasswordMismatch = passwordsStarted && !passwordsMatch;

    const auth = useAuth();
    const goTo = useNavigate();

    async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        if (
            !input.nombreEmpresa.trim() ||
            !input.nombreAdministrador.trim() ||
            !input.emailAdministrador.trim() ||
            !input.passwordAdministrador.trim() ||
            !input.passwordRepeat.trim()
        ) {
            setErrorResponse("Debes rellenar todos los campos.");
            return;
        }

        if (hasPasswordMismatch) {
            setErrorResponse("Las contraseñas no coinciden");
            return;
        }
        setLoading(true);
        try {
            await auth.signup(
                input.nombreEmpresa,
                input.nombreAdministrador,
                input.emailAdministrador,
                input.passwordAdministrador,
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
                        <label htmlFor="nombreEmpresa" className="form-label">Nombre de la Empresa</label>
                        <input type="text" className="form-control" id="nombreEmpresa" placeholder="Nombre de tu empresa"
                        value= {input.nombreEmpresa} 
                        onChange={(e) => setInput({
                            ...input, nombreEmpresa: e.target.value
                            })} />
                    </div>
                    <div className="mb-3">
                        <label htmlFor="nombre" className="form-label">Nombre</label>
                        <input type="text" className="form-control" id="nombre" placeholder="Tu nombre completo"
                        value= {input.nombreAdministrador} 
                        onChange={(e) => setInput({
                            ...input, nombreAdministrador: e.target.value
                            })} />
                    </div>
                    <div className="mb-3">
                        <label htmlFor="email" className="form-label">Email</label>
                        <input type="email" className="form-control" id="email" placeholder="tu@email.com" 
                        value= {input.emailAdministrador} 
                        onChange={(e) => setInput({
                            ...input, 
                            emailAdministrador: e.target.value
                            })} />
                    </div>
                    <div className="mb-3">
                        <label htmlFor="password" className="form-label">Contraseña</label>
                        <input type="password" className="form-control" id="password" placeholder="Ingresa tu contraseña"
                        value= {input.passwordAdministrador}  
                        onChange={(e) => setInput({
                            ...input, passwordAdministrador: e.target.value
                            })} />
                    </div>
                    <div className="mb-3">
                        <label htmlFor="passwordRepeat" className="form-label">Repetir Contraseña</label>
                        <input type="password" className={`form-control ${hasPasswordMismatch ? "is-invalid" : passwordsStarted ? "is-valid" : ""}`} id="passwordRepeat" placeholder="Ingresa tu contraseña"
                        value= {input.passwordRepeat}  
                        onChange={(e) => setInput({
                            ...input, passwordRepeat: e.target.value
                            })} />
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
                    <button type="button" className="btn btn-outline-secondary w-100" onClick={() => window.location.href = "/login"} disabled={loading}>Volver al Inicio de sesión</button>
                </form>
            </div>
        </div>
    )
}