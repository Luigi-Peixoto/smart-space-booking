import { Outlet } from "react-router-dom";
import Navbar from "./navbar/Navbar";

function Layout() {
  return (
    <div className="layout-container">
      <Navbar />
      <main className="layout-content">
        <Outlet />
      </main>
    </div>
  );
}

export default Layout;
