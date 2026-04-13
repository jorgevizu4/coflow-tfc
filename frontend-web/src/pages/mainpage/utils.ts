export function formatDate(d?: string): string {
    if (!d) return "—";
    return new Date(d).toLocaleDateString("es-ES", { day: "2-digit", month: "short", year: "numeric" });
}

export function nowMin(): string {
    const now = new Date();
    now.setSeconds(0, 0);
    return now.toISOString().slice(0, 16);
}

export function closeBsModal(id: string): void {
    (document.getElementById(id) as HTMLButtonElement | null)?.click();
}
