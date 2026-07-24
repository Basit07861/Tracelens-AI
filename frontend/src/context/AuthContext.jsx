import {
  useCallback,
  useMemo,
  useState,
} from "react";

import api, {
  TOKEN_STORAGE_KEY,
  USER_STORAGE_KEY,
} from "../api/client";
import AuthContext from "./auth-context";

function readStoredUser() {
  const storedUser = localStorage.getItem(USER_STORAGE_KEY);

  if (!storedUser) {
    return null;
  }

  try {
    return JSON.parse(storedUser);
  } catch {
    localStorage.removeItem(USER_STORAGE_KEY);
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser);

  const login = useCallback(async (email, password) => {
    const response = await api.post("/api/auth/login", {
      email,
      password,
    });

    const loginData = response.data?.data;

    if (!loginData?.accessToken || !loginData?.user) {
      throw new Error(
        "The server returned an invalid login response.",
      );
    }

    localStorage.setItem(
      TOKEN_STORAGE_KEY,
      loginData.accessToken,
    );

    localStorage.setItem(
      USER_STORAGE_KEY,
      JSON.stringify(loginData.user),
    );

    setUser(loginData.user);

    return loginData.user;
  }, []);

  const register = useCallback(async (registrationData) => {
    const response = await api.post(
      "/api/auth/register",
      registrationData,
    );

    return response.data?.data;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    localStorage.removeItem(USER_STORAGE_KEY);
    setUser(null);
  }, []);

  const isAuthenticated = Boolean(
    user && localStorage.getItem(TOKEN_STORAGE_KEY),
  );

  const value = useMemo(
    () => ({
      user,
      isAuthenticated,
      login,
      register,
      logout,
    }),
    [
      user,
      isAuthenticated,
      login,
      register,
      logout,
    ],
  );

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}