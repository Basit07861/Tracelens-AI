import axios from "axios";

export const TOKEN_STORAGE_KEY = "tracelens_token";
export const USER_STORAGE_KEY = "tracelens_user";

const api = axios.create({
  baseURL:
    import.meta.env.VITE_API_BASE_URL ||
    "http://localhost:8080",
  timeout: 30000,
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(
      TOKEN_STORAGE_KEY,
    );

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error),
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
      localStorage.removeItem(USER_STORAGE_KEY);

      if (window.location.pathname !== "/login") {
        window.location.assign("/login");
      }
    }

    return Promise.reject(error);
  },
);

export default api;