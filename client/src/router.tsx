import { createBrowserRouter } from 'react-router-dom'
import React from 'react'
import Homepage from './pages/Home/Homepage'
import ErrorPage from './pages/Error/ErrorPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Homepage />,
    errorElement: <ErrorPage />
  }
])
