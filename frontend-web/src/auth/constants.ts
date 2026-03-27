const DEFAULT_API_BASE_URL = "http://localhost:8080/api";

const viteApiBaseUrl = (import.meta as ImportMeta & {
    env?: { VITE_API_BASE_URL?: string };
}).env?.VITE_API_BASE_URL;

export const API_BASE_URL = viteApiBaseUrl?.trim() || DEFAULT_API_BASE_URL;