import React from 'react'
import ReactDOM from 'react-dom/client'
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import App from './App.jsx'
import ResultPage from './ResultPage.jsx';
import './index.css'

const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
  },
  {
    path: "/editor",
    element: <App />,
  },
  {
    path: "/editor/:id",
    element: <App />,
  },
  {
    path: "/editor/auction/:id",
    element: <App />,
  },
  {
    path: "/editor/product/:id",
    element: <App />,
  },
  {
    path: "/editor/result/:id",
    element: <ResultPage />,
  },
]);

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>,
)
