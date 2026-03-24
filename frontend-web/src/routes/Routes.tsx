import React from 'react';
import { createBrowserRouter, Navigate, RouterProvider } from 'react-router-dom';
import Login from '../pages/Login';
import PaginaPrincipal from '../pages/MainPage';
import ProtectedRoute from '../pages/ProtectedRoute';
import SignUp from '../pages/SignUp';
import './Routes.css';

const router = createBrowserRouter([
  {
    path: "/login",
    element: <Login />,
  },
  {
    path: "/signup",
    element: <SignUp />,
  },
  {
    path: "/",
    element: <ProtectedRoute />,
    children: [
      {
        index: true,
        element: <Navigate to="/main" replace />,
      },
      {
        path: "main",
        element: <PaginaPrincipal />
      }
    ]
  }
]);

export default function Routes() {
  return <RouterProvider router={router} />;
}
