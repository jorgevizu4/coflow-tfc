import React from "react";
import { Link } from "react-router-dom";

interface DefaultLayoutProps {
    children: React.ReactNode;
}

export default function DefaultLayout({ children }: DefaultLayoutProps) {
    return (
        <div className="default-layout">
            <header>
                <nav className="navbar navbar-expand-lg bg-body-tertiary">
                    <div className="container-fluid">
                        <h3 className="nav-bar brand">CoFlow</h3>
                        <button className="navbar-toggler" type="button" data-bs-toggle="collapse" 
                                data-bs-target="#navbarNavAltMarkup" aria-controls="navbarNavAltMarkup" 
                                aria-expanded="false" aria-label="Toggle navigation">
                            <span className="navbar-toggler-icon"></span>
                        </button>
                        <div className="collapse navbar-collapse" id="navbarNavAltMarkup">
                            <div className="navbar-nav ms-auto pe-3">
                                <Link className="nav-link" to="/main">Home</Link>
                                <Link className="nav-link" to="/login">Logout</Link>
                            </div>
                        </div>
                    </div>
                </nav>
            </header>
            <main>
                {children}
            </main>
            <footer className="text-center p-3">
                &copy; 2026 CoFlow. All rights reserved.
            </footer>
        </div>
    )
}