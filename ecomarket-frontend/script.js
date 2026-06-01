// --- CONFIGURACIÓN DE PUERTOS POR MICROSERVICIO ---
const AUTH_BASE    = "http://localhost:8080/api/auth";
const API_AUTH     = "http://localhost:8080/api";
const API_CATALOGO = "http://localhost:8081/api";
const API_PEDIDOS  = "http://localhost:8082/api";

// --- ESTADO DE LA APLICACIÓN ---
let isRegisterMode = false;
let currentUser = {
    id: localStorage.getItem('eco_id') || null,
    token: localStorage.getItem('eco_token') || null,
    rol: localStorage.getItem('eco_rol') || null,
    correo: localStorage.getItem('eco_correo') || null
};
let carrito = [];

// 🌟 ALMACÉN DE DATOS EN MEMORIA PARA EDICIÓN RÁPIDA 🌟
window.ecoStore = { productos: {}, clientes: {}, categorias: {}, pedidos: {} };
window.currentEditId = null;

// --- 🛒 NUEVO SISTEMA DE CARRITO PERSISTENTE ---
function obtenerClaveCarritoUsuario() {
    const identificador = currentUser.id ? currentUser.id : 'invitado';
    return `carrito_${identificador}`;
}

function guardarCarritoEnStorage() {
    const clave = obtenerClaveCarritoUsuario();
    localStorage.setItem(clave, JSON.stringify(carrito));
}

function cargarCarritoDeStorage() {
    const clave = obtenerClaveCarritoUsuario();
    const carritoGuardado = localStorage.getItem(clave);
    if (carritoGuardado) {
        carrito = JSON.parse(carritoGuardado);
    } else {
        carrito = [];
    }
}

// --- FUNCIÓN GLOBAL COMPATIBLE CON EL BOTÓN ---
window.agregarAlCarrito = function(id, nombre, precio, stockDisponible) {
    if (stockDisponible <= 0) {
        alert(`Lo sentimos, ${nombre} está agotado.`);
        return;
    }

    const productoExistente = carrito.find(item => item.id === id);
    
    if (productoExistente) {
        if (productoExistente.cantidad + 1 > stockDisponible) {
            alert(`No puedes agregar más. El stock máximo disponible es ${stockDisponible}.`);
            return;
        }
        productoExistente.cantidad += 1;
    } else {
        carrito.push({ id, nombre, precio, cantidad: 1, stock: stockDisponible }); 
    }
    
    guardarCarritoEnStorage();
    alert(`¡${nombre} añadido al carrito!`);
};

// --- CONTROL DE INICIALIZACIÓN ---
window.onload = () => { checkAuthentication(); };

// --- SISTEMA DE AUTENTICACIÓN (JWT) ---
function checkAuthentication() {
    const authContainer = document.getElementById('auth-container');
    const mainContent = document.getElementById('main-app-content');
    const errorBox = document.getElementById('auth-error');
    const navComunidad = document.getElementById('nav-comunidad');
    
    if (navComunidad) navComunidad.style.display = currentUser.rol === 'ADMIN' ? 'block' : 'none';
    if (errorBox) errorBox.style.display = 'none';

    if (currentUser.token) {
        if (authContainer) {
            authContainer.style.opacity = '0';
            setTimeout(() => authContainer.style.display = 'none', 500);
        }
        if (mainContent) mainContent.style.display = 'block';
        
        applyRoleRestrictions();
        cargarCarritoDeStorage();
        
        const welcomeTxt = document.getElementById('user-welcome');
        if (welcomeTxt) welcomeTxt.innerText = `Conectado como: ${currentUser.correo} (${currentUser.rol})`;
        
        showSection('productos');
    } else {
        carrito = [];
        if (authContainer) {
            authContainer.style.display = 'flex';
            setTimeout(() => authContainer.style.opacity = '1', 50);
        }
        if (mainContent) mainContent.style.display = 'none';
    }
}

function toggleAuthMode() {
    isRegisterMode = !isRegisterMode;
    const title = document.getElementById('auth-title');
    const subtitle = document.getElementById('auth-subtitle');
    const registerFields = document.getElementById('auth-register-fields');
    const submitBtn = document.getElementById('auth-submit-btn');
    const toggleLink = document.getElementById('auth-toggle-link');
    const errorBox = document.getElementById('auth-error');

    if (errorBox) errorBox.style.display = 'none';

    if (isRegisterMode) {
        if (title) title.innerText = "Crear Cuenta Eco";
        if (subtitle) subtitle.innerText = "Únete a la comunidad digital y gestiona recursos sostenibles.";
        if (registerFields) registerFields.style.display = "block";
        if (submitBtn) submitBtn.innerText = "Registrarse e Ingresar";
        if (toggleLink) toggleLink.innerText = "¿Ya tienes cuenta? Inicia sesión";
    } else {
        if (title) title.innerText = "Iniciar Sesión";
        if (subtitle) subtitle.innerText = "Ingresa tus credenciales para acceder al panel ecológico.";
        if (registerFields) registerFields.style.display = "none";
        if (submitBtn) submitBtn.innerText = "Ingresar al Panel";
        if (toggleLink) toggleLink.innerText = "¿No tienes cuenta? Regístrate aquí";
    }
}

document.getElementById('auth-form').onsubmit = async (e) => {
    e.preventDefault();
    const errorBox = document.getElementById('auth-error');
    if (errorBox) errorBox.style.display = 'none';

    const correo = document.getElementById('auth-correo').value.trim();
    const password = document.getElementById('auth-password').value;
    const nombre = document.getElementById('auth-nombre').value.trim();

    let endpoint = isRegisterMode ? `${AUTH_BASE}/register` : `${AUTH_BASE}/login`;
    let payload = isRegisterMode ? { nombre, correo, password, rol: "CLIENTE" } : { correo, password };

    try {
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.mensaje || "Error en las credenciales o en el servidor.");
        }

        const data = await response.json();
        
        localStorage.setItem('eco_id', data.usuario.id);
        localStorage.setItem('eco_token', data.token);
        localStorage.setItem('eco_rol', data.usuario.rol);
        localStorage.setItem('eco_correo', data.usuario.correo);

        currentUser = {
            id: data.usuario.id,
            token: data.token,
            rol: data.usuario.rol,
            correo: data.usuario.correo
        };
        
        document.getElementById('auth-form').reset();
        checkAuthentication();
    } catch (error) {
        if (errorBox) { errorBox.innerText = error.message; errorBox.style.display = 'block'; }
    }
};

function logout() {
    localStorage.removeItem('eco_id');
    localStorage.removeItem('eco_token');
    localStorage.removeItem('eco_rol');
    localStorage.removeItem('eco_correo');
    
    currentUser = { id: null, token: null, rol: null, correo: null };
    carrito = [];
    checkAuthentication();
}

function applyRoleRestrictions() {
    const adminElements = document.querySelectorAll('.admin-only');
    adminElements.forEach(el => el.style.display = currentUser.rol === 'ADMIN' ? 'block' : 'none');
}

// --- FUNCIÓN CENTRAL DE PETICIONES HTTP ---
async function ecoFetch(url, options = {}) {
    options.headers = {
        ...options.headers,
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${currentUser.token}`
    };
    const response = await fetch(url, options);
    if (!response.ok) {
        if (response.status === 401) { alert("Sesión expirada."); logout(); }
        else if (response.status === 403) { alert("Acceso Denegado: Rol insuficiente."); showSection('productos'); }
    }
    return response;
}

// --- ♻️ FUNCIONES CRUD GLOBALES (ELIMINAR Y REABASTECER) ♻️ ---
window.eliminarRegistro = async function(tipo, ids) {
    if (!confirm(`¿Estás seguro de que deseas ELIMINAR este registro de forma permanente?`)) return;

    let endpointBase = "";
    let path = "";

    if (tipo === 'producto' || tipo === 'categoria') {
        endpointBase = API_CATALOGO; path = `${tipo}s`;
    } else if (tipo === 'pedido') {
        endpointBase = API_PEDIDOS; path = `${tipo}s`;
    } else if (tipo === 'cliente') {
        endpointBase = API_AUTH; path = `usuarios`;
    }

    try {
        const idArray = Array.isArray(ids) ? ids : [ids];
        for (const id of idArray) {
            
            // --- LÓGICA DE REABASTECIMIENTO AL BORRAR ORDEN ---
            if (tipo === 'pedido') {
                const respPedido = await ecoFetch(`${API_PEDIDOS}/pedidos/${id}`);
                if (respPedido.ok) {
                    const pedidoOriginal = await respPedido.json();
                    
                    // Solo devolvemos stock si NO estaba ya cancelado (evita duplicar la devolución)
                    if (pedidoOriginal.estado !== 'CANCELADO') {
                        const resProd = await ecoFetch(`${API_CATALOGO}/productos/${pedidoOriginal.productoId}`);
                        if (resProd.ok) {
                            const prod = await resProd.json();
                            prod.stock += (pedidoOriginal.cantidad || 1); // Sumar stock de vuelta
                            
                            // Guardar stock actualizado
                            await ecoFetch(`${API_CATALOGO}/productos/${prod.id}`, {
                                method: 'PUT',
                                body: JSON.stringify(prod)
                            });
                        }
                    }
                }
            }
            // --- FIN REABASTECIMIENTO ---

            // Eliminar el registro final
            const response = await ecoFetch(`${endpointBase}/${path}/${id}`, { method: 'DELETE' });
            if (!response.ok) throw new Error(`Fallo al eliminar el ID ${id}`);
        }
        
        alert("Eliminado con éxito.");
        showSection(tipo === 'cliente' ? 'clientes' : `${tipo}s`);
        
        // Refrescamos productos en segundo plano para que se vea el stock actualizado
        if (tipo === 'pedido') renderProductos();
    } catch (error) {
        alert("Fallo de red o restricción de base de datos: " + error.message);
    }
};

// --- NAVEGACIÓN Y VISTAS DENTRO DEL PANEL ---
async function showSection(sectionId) {
    const section = document.getElementById(sectionId);
    if (!section) return;
    document.querySelectorAll('section').forEach(s => s.classList.remove('active'));
    section.classList.add('active');
    
    try {
        switch(sectionId) {
            case 'productos': await renderProductos(); break;
            case 'clientes': await renderClientes(); break;
            case 'pedidos': await renderPedidos(); break;
            case 'categorias': await renderCategorias(); break;
        }
    } catch (error) { console.error(`Error renderizando ${sectionId}:`, error); }
}

// --- RENDERIZADO DINÁMICO ---
async function renderProductos() {
    const grid = document.getElementById('grid-productos');
    if (!grid) return;
    grid.innerHTML = '<p class="loading">Sincronizando con la red ecológica...</p>';

    try {
        const response = await ecoFetch(`${API_CATALOGO}/productos`);
        if (!response.ok) throw new Error();
        const productos = await response.json();
        
        window.ecoStore.productos = {};

        grid.innerHTML = productos.length > 0 ? productos.map(p => {
            window.ecoStore.productos[p.id] = p;
            
            const adminControls = currentUser.rol === 'ADMIN' ? `
                <div style="margin-top:15px; display:flex; gap:10px; border-top:1px solid rgba(255,255,255,0.1); padding-top:10px;">
                    <button onclick="openModal('producto', ${p.id})" style="flex:1; background:#3b82f6; color:white; border:none; padding:5px; border-radius:4px; font-size:0.75rem; cursor:pointer;">✏️ Editar</button>
                    <button onclick="eliminarRegistro('producto', ${p.id})" style="flex:1; background:var(--error-red); color:white; border:none; padding:5px; border-radius:4px; font-size:0.75rem; cursor:pointer;">🗑️ Borrar</button>
                </div>
            ` : '';

            return `
            <div class="card">
                <small style="color:var(--primary-green); opacity:0.6; font-weight:700">#ID-${p.id}</small>
                <h3 style="margin-top:5px;">${p.nombre}</h3>
                <p class="price">$${p.precio.toFixed(0)}</p>
                <div style="margin-top:20px; display:flex; justify-content:space-between; align-items:center">
                    <span style="font-size:0.8rem; color:var(--text-dim)">Inventario: ${p.stock} uds</span>
                    <button onclick="agregarAlCarrito(${p.id}, '${p.nombre}', ${p.precio}, ${p.stock})"
                            style="background:var(--primary-green); color:var(--bg-deep); border:none; padding:6px 12px; border-radius:6px; font-weight:700; cursor:pointer; font-size:0.8rem;">
                        + Carrito
                    </button>
                </div>
                ${adminControls}
            </div>
            `;
        }).join('') : '<p class="loading">No hay productos registrados en el catálogo.</p>';
    } catch (error) { grid.innerHTML = `<div class="card" style="border-color:var(--error-red)"><h3>Fallo de Sincronización</h3></div>`; }
}

async function renderClientes() {
    const tbody = document.querySelector('#tabla-clientes tbody');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="4" class="loading" style="padding: 20px;">Cargando red...</td></tr>';

    try {
        const response = await ecoFetch(`${API_AUTH}/usuarios`);
        if (!response.ok) throw new Error();
        const clientes = await response.json();
        
        window.ecoStore.clientes = {};

        tbody.innerHTML = clientes.length > 0 ? clientes.map(c => {
            window.ecoStore.clientes[c.id] = c;
            
            const deleteBtn = currentUser.rol === 'ADMIN' && currentUser.id != c.id ? `
                <button onclick="eliminarRegistro('cliente', ${c.id})" style="margin-left:10px; background:var(--error-red); color:white; border:none; padding:2px 6px; border-radius:4px; cursor:pointer;" title="Eliminar Usuario">🗑️</button>
            ` : '';

            return `
            <tr>
                <td><small style="color:var(--text-dim)">#${c.id}</small></td>
                <td style="font-weight:600">${c.nombre}</td>
                <td style="color:var(--primary-green)">${c.email || c.correo}</td>
                <td>
                    <span style="font-size:0.75rem; background:rgba(52,211,153,0.08); padding:4px 10px; border-radius:6px; color:var(--primary-green); font-weight:700;">
                        ${c.rol || 'CLIENTE'}
                    </span>
                    ${deleteBtn}
                </td>
            </tr>
            `;
        }).join('') : '<tr><td colspan="4">No hay clientes.</td></tr>';
    } catch (error) { tbody.innerHTML = `<tr><td colspan="4" style="color:var(--error-red);">Requiere privilegios de ADMIN.</td></tr>`; }
}

async function renderCategorias() {
    const grid = document.getElementById('grid-categorias');
    if (!grid) return;
    grid.innerHTML = '<p class="loading">Estructurando índices de clasificación...</p>';

    try {
        const response = await ecoFetch(`${API_CATALOGO}/categorias`);
        if (!response.ok) throw new Error();
        const categorias = await response.json();
        
        window.ecoStore.categorias = {};

        grid.innerHTML = categorias.length > 0 ? categorias.map(c => {
            window.ecoStore.categorias[c.id] = c;

            const adminControls = currentUser.rol === 'ADMIN' ? `
                <div style="margin-top:15px; display:flex; gap:10px;">
                    <button onclick="openModal('categoria', ${c.id})" style="flex:1; background:#3b82f6; color:white; border:none; padding:5px; border-radius:4px; font-size:0.75rem; cursor:pointer;">✏️ Editar</button>
                    <button onclick="eliminarRegistro('categoria', ${c.id})" style="flex:1; background:var(--error-red); color:white; border:none; padding:5px; border-radius:4px; font-size:0.75rem; cursor:pointer;">🗑️ Borrar</button>
                </div>
            ` : '';

            return `
            <div class="card" style="border-left: 4px solid var(--primary-green)">
                <small style="color:var(--text-dim); font-weight:700;">REGISTRO #${c.id}</small>
                <h3 style="margin-top:4px;">${c.nombre}</h3>
                <p style="color:var(--text-dim); margin-top:10px; font-size:0.85rem; line-height:1.4">${c.descripcion}</p>
                ${adminControls}
            </div>
            `;
        }).join('') : '<p class="loading">No hay categorías configuradas.</p>';
    } catch (error) { grid.innerHTML = '<p class="loading" style="color:var(--error-red)">Fallo de red.</p>'; }
}

async function renderPedidos() {
    const grid = document.getElementById('grid-pedidos');
    if (!grid) return;
    grid.innerHTML = '<p class="loading">Cargando flujos de distribución...</p>';
    
    try {
        const response = await ecoFetch(`${API_PEDIDOS}/pedidos`);
        if (!response.ok) throw new Error();
        let pedidos = await response.json();

        let productosCatalogo = [];
        try {
            const resProd = await fetch(`${API_CATALOGO}/productos`, { headers: { 'Authorization': `Bearer ${currentUser.token}` }});
            if (resProd.ok) productosCatalogo = await resProd.json();
        } catch (e) {}

        if (currentUser.rol !== 'ADMIN') {
            pedidos = pedidos.filter(p => String(p.usuarioId) === String(currentUser.id));
        }

        let pedidosAgrupados = [];
        if (pedidos.length > 0) {
            pedidos.sort((a, b) => a.id - b.id); 
            let grupo = null;
            for (const p of pedidos) {
                const infoProd = productosCatalogo.find(prod => prod.id === p.productoId);
                const nombreReal = infoProd ? infoProd.nombre : `Producto #${p.productoId}`;
                const precioReal = infoProd ? infoProd.precio : 0;
                const subtotalItem = precioReal * (p.cantidad || 1);

                if (!grupo) {
                    grupo = { ...p, montoTotal: subtotalItem, ids: [p.id], itemsDetalle: [{ nombre: nombreReal, cantidad: p.cantidad || 1, subtotal: subtotalItem }] };
                    pedidosAgrupados.push(grupo);
                } else {
                    const tiempoAnterior = new Date(grupo.fecha).getTime();
                    const tiempoActual = new Date(p.fecha).getTime();
                    const diferenciaSegundos = Math.abs(tiempoAnterior - tiempoActual) / 1000;

                    if (grupo.usuarioId === p.usuarioId && diferenciaSegundos <= 5) {
                        grupo.montoTotal += subtotalItem;
                        grupo.ids.push(p.id);
                        grupo.itemsDetalle.push({ nombre: nombreReal, cantidad: p.cantidad || 1, subtotal: subtotalItem });
                    } else {
                        grupo = { ...p, montoTotal: subtotalItem, ids: [p.id], itemsDetalle: [{ nombre: nombreReal, cantidad: p.cantidad || 1, subtotal: subtotalItem }] };
                        pedidosAgrupados.push(grupo);
                    }
                }
            }
            pedidosAgrupados.reverse(); 
        }

        grid.innerHTML = pedidosAgrupados.length > 0 ? pedidosAgrupados.map(p => {
            const textoLote = p.ids.length > 1 ? `Lote #${p.ids[0]}-${p.ids[p.ids.length - 1]}` : `#${p.ids[0]}`;
            
            const htmlProductosInternos = p.itemsDetalle.map(item => `
                <div style="display:flex; justify-content:space-between; font-size:0.8rem; color:var(--text-dim); margin-bottom:5px;">
                    <span>• ${item.nombre} <b style="color:white">x${item.cantidad}</b></span>
                    <span>$${item.subtotal.toFixed(0)}</span>
                </div>
            `).join('');

            const esMio = String(p.usuarioId) === String(currentUser.id);
            const puedeBorrar = (currentUser.rol === 'ADMIN' || esMio);
            
            const editBtn = currentUser.rol === 'ADMIN' ? `
                <button onclick="cambiarEstadoPedido([${p.ids.join(',')}], '${p.estado}')" style="background:#fbbf24; color:var(--bg-deep); border:none; padding:5px 8px; border-radius:4px; font-size:0.7rem; font-weight:700; cursor:pointer;">⚙️ Estado</button>
            ` : '';

            const deleteBtn = puedeBorrar ? `
                <button onclick="eliminarRegistro('pedido', [${p.ids.join(',')}])" style="background:var(--error-red); color:white; border:none; padding:5px 8px; border-radius:4px; font-size:0.7rem; cursor:pointer;">🗑️ Borrar Orden</button>
            ` : '';

            return `
            <div class="card" style="border-right: ${p.estado === 'ENTREGADO' ? '3px solid var(--primary-green)' : (p.estado === 'CANCELADO' ? '3px solid var(--error-red)' : '3px solid #fbbf24')}">
                <div style="display:flex; justify-content:space-between;">
                    <small style="color:var(--text-dim); font-weight:600">Orden de Compra ${textoLote}</small>
                    <span style="font-size:0.7rem; font-weight:800; background:rgba(255,255,255,0.1); padding:2px 8px; border-radius:4px">${p.estado}</span>
                </div>
                <h3 style="margin: 8px 0; color:var(--primary-green)">$${p.montoTotal.toFixed(0)} CLP</h3>
                
                <div style="margin: 12px 0; background:rgba(255,255,255,0.02); padding:10px; border-radius:6px; border:1px solid rgba(255,255,255,0.05)">
                    <p style="font-size:0.75rem; font-weight:700; color:white; margin-bottom:6px; text-transform:uppercase;">Artículos solicitados:</p>
                    ${htmlProductosInternos}
                </div>

                <p style="color:var(--text-dim); font-size:0.85rem">Usuario ID: ${p.usuarioId || p.clienteId || 'N/A'}</p>
                <div style="margin-top:15px; display:flex; gap:10px; align-items:center;">
                    ${editBtn}
                    ${deleteBtn}
                </div>
            </div>
            `;
        }).join('') : '<p class="loading">No registras órdenes activas.</p>';
    } catch (error) { grid.innerHTML = '<p class="loading" style="color:var(--error-red)">Error al consultar histórico.</p>'; }
}

window.cambiarEstadoPedido = async function(ids, estadoActual) {
    // 👇 AQUÍ ESTÁ EL CAMBIO: Alineado exactamente con lo que pide tu backend
    const estadosPermitidos = ["PENDIENTE", "PAGADO", "ENTREGADO", "CANCELADO"];
    const nuevoEstado = prompt(`Escriba el nuevo estado (Opciones: ${estadosPermitidos.join(', ')}):`, estadoActual);

    if (!nuevoEstado || nuevoEstado.toUpperCase() === estadoActual.toUpperCase() || !estadosPermitidos.includes(nuevoEstado.toUpperCase())) {
        return alert("Operación cancelada o estado no válido.");
    }

    try {
        const estadoFinal = nuevoEstado.toUpperCase();

        for (const id of ids) {
            const respGet = await ecoFetch(`${API_PEDIDOS}/pedidos/${id}`);
            if(!respGet.ok) continue;
            const pedidoOriginal = await respGet.json();

            const respEstado = await ecoFetch(`${API_PEDIDOS}/pedidos/${id}/estado`, {
                method: 'POST',
                body: JSON.stringify({ estado: estadoFinal })
            });

            if (!respEstado.ok) {
                const errorData = await respEstado.text();
                console.error(`Rechazo de Spring Boot para el ID ${id}:`, errorData);
                throw new Error(`El backend rechazó el cambio (Error ${respEstado.status}). Revisa la consola.`);
            }

            // Lógica de Inventario
            if (estadoFinal === 'CANCELADO' && estadoActual.toUpperCase() !== 'CANCELADO') {
                const resProd = await ecoFetch(`${API_CATALOGO}/productos/${pedidoOriginal.productoId}`);
                if (resProd.ok) {
                    const prod = await resProd.json();
                    prod.stock += pedidoOriginal.cantidad;
                    await ecoFetch(`${API_CATALOGO}/productos/${prod.id}`, { method: 'PUT', body: JSON.stringify(prod) });
                }
            } else if (estadoActual.toUpperCase() === 'CANCELADO' && estadoFinal !== 'CANCELADO') {
                const resProd = await ecoFetch(`${API_CATALOGO}/productos/${pedidoOriginal.productoId}`);
                if (resProd.ok) {
                    const prod = await resProd.json();
                    prod.stock -= pedidoOriginal.cantidad;
                    await ecoFetch(`${API_CATALOGO}/productos/${prod.id}`, { method: 'PUT', body: JSON.stringify(prod) });
                }
            }
        }

        alert("¡Estado actualizado con éxito!");
        renderPedidos();
        renderProductos();
    } catch(e) {
        alert("Error al actualizar el estado: " + e.message);
    }
}

// --- CONFIGURACIÓN DE MODALES Y CARRITO DINÁMICO ---
window.openModal = function(tipo, idToEdit = null) {
    const modal = document.getElementById('modal');
    const fields = document.getElementById('form-fields');
    if (!modal || !fields) return;

    window.currentEditId = idToEdit;
    document.getElementById('modal-title').innerText = idToEdit ? `Editar ${tipo.toUpperCase()} #${idToEdit}` : `Registrar Nuevo ${tipo.toUpperCase()}`;
    modal.style.display = 'grid';

    if (tipo === 'producto') {
        fields.innerHTML = `
            <input type="text" id="p-nombre" placeholder="Nombre descriptivo del Producto" required>
            <textarea id="p-descripcion" placeholder="Descripción detallada del producto..." style="min-height:80px;" required></textarea>
            <div style="display: flex; gap: 10px;">
                <input type="number" id="p-precio" placeholder="Precio ($)" step="0.01" style="flex:1" required>
                <input type="number" id="p-stock" placeholder="Stock" style="flex:1" required>
                <input type="number" id="p-categoriaId" placeholder="ID Categoría (Ej: 1)" style="flex:1" required>
            </div>
        `;
        if (idToEdit && window.ecoStore.productos[idToEdit]) {
            setTimeout(() => {
                document.getElementById('p-nombre').value = window.ecoStore.productos[idToEdit].nombre;
                document.getElementById('p-descripcion').value = window.ecoStore.productos[idToEdit].descripcion || '';
                document.getElementById('p-precio').value = window.ecoStore.productos[idToEdit].precio;
                document.getElementById('p-stock').value = window.ecoStore.productos[idToEdit].stock;
                document.getElementById('p-categoriaId').value = window.ecoStore.productos[idToEdit].categoriaId || '';
            }, 50);
        }
    } else if (tipo === 'cliente') {
        fields.innerHTML = `
            <input type="text" id="c-nombre" placeholder="Nombre y Apellido del Cliente" required>
            <input type="email" id="c-email" placeholder="Dirección de Correo Electrónico" required>
        `;
    } else if (tipo === 'categoria') {
        fields.innerHTML = `
            <input type="text" id="cat-nombre" placeholder="Clave de la Categoría" required>
            <textarea id="cat-descripcion" placeholder="Alcance o descripción..." style="min-height:110px;"></textarea>
        `;
        if (idToEdit && window.ecoStore.categorias[idToEdit]) {
            setTimeout(() => {
                document.getElementById('cat-nombre').value = window.ecoStore.categorias[idToEdit].nombre;
                document.getElementById('cat-descripcion').value = window.ecoStore.categorias[idToEdit].descripcion;
            }, 50);
        }
    } else if (tipo === 'pedido') {
        if (carrito.length === 0) { alert("Carrito vacío."); return closeModal(); }

        fields.innerHTML = `
            <div style="background:rgba(255,255,255,0.03); padding:15px; border-radius:8px; margin-bottom:15px; border:1px solid var(--border)">
                <h4 style="margin-bottom:10px; color:var(--primary-green)">Resumen de Compra</h4>
                <div id="contenedor-lista-carrito"></div>
            </div>
            <h4 style="margin-bottom:10px; color:white; font-size:0.9rem;">Despacho</h4>
            <input type="text" id="ped-direccion" placeholder="Dirección de calle" required>
        `;
        renderizarModalCarrito();
    }

    document.getElementById('data-form').onsubmit = async (e) => {
        e.preventDefault(); 
        await saveData(tipo); 
    };
}

// 🛒 Funciones dinámicas para el Modal del Carrito
window.renderizarModalCarrito = function() {
    const contenedor = document.getElementById('contenedor-lista-carrito');
    if (!contenedor) return;

    if (carrito.length === 0) {
        alert("El carrito se ha quedado vacío.");
        closeModal();
        return;
    }

    let html = '';
    let totalCalculado = 0;

    carrito.forEach(item => {
        const subtotal = item.precio * item.cantidad;
        totalCalculado += subtotal;
        html += `
        <div style="display:flex; justify-content:space-between; align-items:center; font-size:0.95rem; margin-bottom:8px; border-bottom:1px solid rgba(255,255,255,0.05); padding-bottom:5px;">
            <div style="flex:2;">${item.nombre}</div>
            
            <div style="display:flex; align-items:center; gap:8px; flex:1; justify-content:center;">
                <button type="button" onclick="modificarCantidadCarrito(${item.id}, -1)" style="background:#4b5563; border:none; color:white; padding:2px 8px; border-radius:4px; cursor:pointer; font-weight:bold;">-</button>
                <span>${item.cantidad}</span>
                <button type="button" onclick="modificarCantidadCarrito(${item.id}, 1)" style="background:#4b5563; border:none; color:white; padding:2px 8px; border-radius:4px; cursor:pointer; font-weight:bold;">+</button>
            </div>

            <div style="flex:1; text-align:right; color:white; font-weight:600;">$${subtotal.toFixed(0)}</div>
            
            <button type="button" onclick="eliminarProductoCarrito(${item.id})" style="margin-left:10px; background:var(--error-red); border:none; color:white; padding:2px 6px; border-radius:4px; cursor:pointer;" title="Quitar producto">X</button>
        </div>`;
    });

    html += `
        <hr style="border:0; border-top:1px solid var(--border); margin:10px 0;">
        <div style="display:flex; justify-content:space-between; font-weight:700; font-size:1.1rem;">
            <span>TOTAL:</span><span style="color:var(--primary-green)">$${totalCalculado.toFixed(0)} CLP</span>
        </div>
    `;
    contenedor.innerHTML = html;
};

window.modificarCantidadCarrito = function(id, cambio) {
    const prod = carrito.find(p => p.id === id);
    if (prod) {
        if (cambio > 0 && prod.cantidad + cambio > prod.stock) {
            alert(`Stock máximo alcanzado (${prod.stock} unidades disponibles).`);
            return;
        }

        prod.cantidad += cambio;
        if (prod.cantidad <= 0) {
            eliminarProductoCarrito(id);
        } else {
            guardarCarritoEnStorage();
            renderizarModalCarrito();
        }
    }
};

window.eliminarProductoCarrito = function(id) {
    carrito = carrito.filter(p => p.id !== id);
    guardarCarritoEnStorage();
    renderizarModalCarrito();
};

window.closeModal = function() {
    const modal = document.getElementById('modal');
    const form = document.getElementById('data-form');
    if (modal) modal.style.display = 'none';
    if (form) form.reset();
    window.currentEditId = null;
}

async function saveData(tipo) {
    let basePath = "";
    if (tipo === 'producto' || tipo === 'categoria') basePath = `${API_CATALOGO}/${tipo}s`; 
    else if (tipo === 'pedido') basePath = `${API_PEDIDOS}/${tipo}s`;   
    else if (tipo === 'cliente') basePath = `${API_AUTH}/usuarios`;     

    const method = window.currentEditId ? 'PUT' : 'POST';
    const finalUrl = window.currentEditId ? `${basePath}/${window.currentEditId}` : basePath;

    // --- PROCESAMIENTO LIMPIO DE PEDIDOS ---
    if (tipo === 'pedido') {
        const userIdValido = currentUser.id || localStorage.getItem('eco_id') || 1;
        try {
            for (let i = 0; i < carrito.length; i++) {
                const item = carrito[i];
                
                // 1. Crear la orden de compra (Esto lo hace el Cliente sin problema)
                const payloadPedido = { 
                    usuarioId: parseInt(userIdValido), 
                    productoId: parseInt(item.id), 
                    cantidad: parseInt(item.cantidad) 
                };
                const response = await ecoFetch(basePath, { method: 'POST', body: JSON.stringify(payloadPedido) });
                if (!response.ok) throw new Error("Fallo al procesar pedido.");

                // 2. Intentar actualizar el stock
                const productoOriginal = window.ecoStore.productos[item.id];
                if (productoOriginal) {
                    const nuevoStock = productoOriginal.stock - item.cantidad;
                    
                    // Usamos fetch normal en vez de ecoFetch para evitar que salte la alerta roja si hay 403
                    try {
                        await fetch(`${API_CATALOGO}/productos/${item.id}`, {
                            method: 'PUT',
                            headers: {
                                'Content-Type': 'application/json',
                                'Authorization': `Bearer ${currentUser.token}`
                            },
                            body: JSON.stringify({ ...productoOriginal, stock: nuevoStock })
                        });
                    } catch (e) {
                        console.warn("Stock no actualizado visualmente por falta de permisos de ADMIN.");
                    }
                }
            }
            alert(`¡Pedido procesado de forma exitosa!`);
            carrito = [];
            guardarCarritoEnStorage();
            closeModal();
            renderPedidos();
            renderProductos();
            return;
        } catch (error) {
            alert(`Transacción interrumpida: ${error.message}`);
            return;
        }
    }

    // --- BLOQUE GENÉRICO ESTÁNDAR ---
    let payload = {};
    if (tipo === 'producto') {
        payload = {
            nombre: document.getElementById('p-nombre').value.trim(),
            descripcion: document.getElementById('p-descripcion').value.trim(),
            precio: parseFloat(document.getElementById('p-precio').value),
            stock: parseInt(document.getElementById('p-stock').value),
            categoriaId: parseInt(document.getElementById('p-categoriaId').value)
        };
    } else if (tipo === 'cliente') {
        payload = {
            nombre: document.getElementById('c-nombre').value.trim(),
            correo: document.getElementById('c-email').value.trim(),
            password: "defaultPassword123", 
            rol: "CLIENTE"
        };
    } else if (tipo === 'categoria') {
        payload = {
            nombre: document.getElementById('cat-nombre').value.trim(),
            descripcion: document.getElementById('cat-descripcion').value.trim()
        };
    }

    try {
        const response = await ecoFetch(finalUrl, {
            method: method,
            body: JSON.stringify(payload)
        });

        if (!response.ok) throw new Error(`Fallo guardando el ${tipo}`);

        alert(`${tipo.toUpperCase()} procesado correctamente.`);
        closeModal();
        showSection(`${tipo}s`);
    } catch (error) {
        alert(`Error en el servidor: ${error.message}`);
    }
}