import React, { useState } from 'react';
import { useAuth } from '../auth/AuthProvider';
import { Navigate, useNavigate } from 'react-router-dom';

export default function Login() {

    const [input, setInput] = useState({
        email: "",
        password: "",
    });

    const auth = useAuth();
    const goTo = useNavigate();

    const [errorResponse, setErrorResponse] = useState("");
    const [loading, setLoading] = useState(false);

    async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setLoading(true);
        try {
            await auth.login(input.email, input.password);
            setErrorResponse("");
            goTo("/main");
        } catch (error: any) {
            setErrorResponse(error.message || "Error al iniciar sesión");
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
                        <label htmlFor="email" className="form-label">Correo electrónico</label>
                        <input type="email" className="form-control" id="email" placeholder="Ingresa tu correo"
                        value= {input.email} 
                        onChange={(e) => setInput({
                            ...input, email: e.target.value
                            })} />
                    </div>
                    <div className="mb-3">
                        <label htmlFor="password" className="form-label">Contraseña</label>
                        <input type="password" className="form-control" id="password" placeholder="Ingresa tu contraseña"
                        value= {input.password} 
                        onChange={(e) => setInput({
                            ...input, password: e.target.value
                            })} />
                    </div>
                    <button type="submit" className="btn btn-primary w-100 mb-3" disabled={loading}>
                        {loading ? "Cargando..." : "Iniciar sesión"}
                    </button>
                    <button type="button" className="btn btn-outline-secondary w-100" onClick={() => window.location.href = "/signup"} disabled={loading}>Registrarse</button>
                </form>
            </div>
        </div>
    );
}
