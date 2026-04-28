const TOKEN_KEY = "orbitrack.token";
const AUTH_EVENT = "auth-state-changed";
const API_BASE = import.meta.env.VITE_API_BASE || "http://localhost:8080";

export class ApiError extends Error {
    constructor(message, status, data) {
        super(message);
        this.name = "ApiError";
        this.status = status;
        this.data = data;
    }
}

export function getToken() {
    return window.localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
    window.localStorage.setItem(TOKEN_KEY, token);
    window.dispatchEvent(
        new CustomEvent(AUTH_EVENT, {
            detail: {token},
        })
    );
}

export function clearToken() {
    window.localStorage.removeItem(TOKEN_KEY);
    window.dispatchEvent(
        new CustomEvent(AUTH_EVENT, {
            detail: {
                token: null
            }
        })
    );
}

export function subscribeToAuthStateChange(callback) {
    function handleAuthChange(event) {
        callback(event.detail?.token ?? getToken());
    }

    window.addEventListener(AUTH_EVENT, handleAuthChange);
    return () => {
        window.removeEventListener(AUTH_EVENT, handleAuthChange);
    };
}

export async function apiFetch(path, options = {}) {
    const token = getToken();
    const headers = new Headers(options.headers || {});

    if (!headers.has("Content-Type") && options.body) {
        headers.set("Content-Type", "application/json");
    }
    if (token) {
        headers.set("Authorization", `Bearer ${token}`);
    }
    const response = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers
    });
    const contentType = response.headers.get("content-type") || "";
    const isJson = contentType.includes(`application/json`);

    const data = isJson ? await response.json() : await response.text();

    if(!response.ok) {
        const message = typeof data === "object" && data != null && "error" in data ? data.error : `Request failed with status ${response.status}`;

        throw new ApiError(message, response.status, data);
    }
    return data;
}