import { createBrowserRouter } from 'react-router-dom'
import React from 'react'
import Homepage from './pages/Home/Homepage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Homepage />
  }
])
