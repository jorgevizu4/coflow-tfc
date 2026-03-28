import React from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthProvider";

interface DefaultLayoutProps {
    children: React.ReactNode;
}

export default function DefaultLayout({ children }: DefaultLayoutProps) {
    const { logout, user } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate("/login");
    };

    return (
        <div className="d-flex flex-column min-vh-100">
            <header>
                <nav className="navbar navbar-expand-lg  navbar-dark shadow-sm" style={{ background: "var(--deep-space-blue)" }}>
                    <div className="container-fluid">
                        <span className="navbar-brand fw-bold fs-4">
                            <i className="bi bi-diagram-3-fill me-2"></i>CoFlow
                        </span>
                        <button
                            className="navbar-toggler"
                            type="button"
                            data-bs-toggle="collapse"
                            data-bs-target="#navbarContent"
                            aria-controls="navbarContent"
                            aria-expanded="false"
                            aria-label="Toggle navigation"
                        >
                            <span className="navbar-toggler-icon"></span>
                        </button>
                        <div className="collapse navbar-collapse" id="navbarContent">
                            <ul className="navbar-nav ms-auto align-items-center gap-2">
                                {user && (
                                    <li className="nav-item">
                                        <span className="navbar-text text-white-50 small">
                                            <i className="bi bi-person-circle me-1"></i>
                                            {user.email}
                                        </span>
                                    </li>
                                )}
                                <li className="nav-item">
                                    <button
                                        className="btn btn-outline-light btn-sm"
                                        onClick={handleLogout}
                                    >
                                        <i className="bi bi-box-arrow-right me-1"></i>
                                        Cerrar sesión
                                    </button>
                                </li>
                            </ul>
                        </div>
                    </div>
                </nav>
            </header>

            <main className="flex-grow-1 bg-light">
                {children}
            </main>

            <footer className="text-white text-center py-3 small" style={{ background: "var(--deep-space-blue)" }}>
                &copy; 2026 CoFlow. All rights reserved.
            </footer>
        </div>
    );
}