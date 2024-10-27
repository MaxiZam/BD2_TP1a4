package ar.unrn.tp.servicio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ar.unrn.tp.dto.CategoriaDTO;
import ar.unrn.tp.exception.OptimisticLockException;
import ar.unrn.tp.mapper.ProductoMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.unrn.tp.api.ProductoService;
import ar.unrn.tp.modelo.Categoria;
import ar.unrn.tp.modelo.Producto;

@Service
@Transactional
public class ProductoServiceJPA implements ProductoService {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ProductoMapper productoMapper;

    @Override
    public void crearProducto(String codigo, String descripcion, String marca, float precio, Long idCategoria) {
        if (codigoYaExistente(codigo)) {
            throw new IllegalArgumentException("Ya existe un producto con ese código");
        }

        Categoria categoria = em.find(Categoria.class, idCategoria);
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría especificada no existe");
        }

        Producto producto = new Producto(codigo, descripcion, categoria, marca, BigDecimal.valueOf(precio));
        em.persist(producto);
    }

    @Override
    public void modificarProducto(Long idProducto, String nombre, float precio, Long idCategoria, Long version) {
        try {
            Producto producto = em.find(Producto.class, idProducto);
            if (producto == null) {
                throw new IllegalArgumentException("El producto especificado no existe");
            }

            Categoria categoria = em.find(Categoria.class, idCategoria);
            if (categoria == null) {
                throw new IllegalArgumentException("La categoría especificada no existe");
            }

            // Comprobación de la versión
            if (!producto.getVersion().equals(version)) {
                throw new OptimisticLockException("El producto ha sido modificado por otro usuario. Por favor, actualiza la página.");
            }

            // Actualización de los campos
            producto.setNombre(nombre);
            producto.setPrecio(BigDecimal.valueOf(precio));
            producto.setCategoria(categoria);

            em.merge(producto);
        } catch (OptimisticLockException e) {
            throw new OptimisticLockException("El producto ha sido modificado por otro usuario. Por favor, actualiza la página.");
        }
    }


    @Override
    public List<Producto> listarProductos() {
        return em.createQuery("SELECT p FROM Producto p", Producto.class).getResultList();
    }

    @Override
    public void borrarProducto(Long id) {
        em.createQuery("DELETE FROM Producto p WHERE p.id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }

    @Override
    public void crearCategoria(String nombre) {
        if(nombre.isEmpty()){
            throw new IllegalArgumentException("No se ingreso ningun nombre para la categoria");
        }
        Categoria nuevaCategoria = new Categoria(nombre);
        em.persist(nuevaCategoria);
    }

    private boolean codigoYaExistente(String codigo) {
        return !em.createQuery("SELECT p FROM Producto p WHERE p.codigo = :codigo", Producto.class)
                .setParameter("codigo", codigo)
                .getResultList()
                .isEmpty();
    }

    // Función para obtener los productos a partir de una lista de IDs
    public List<Producto> obtenerProductosPorIds(List<Long> productosIds) {
        if (productosIds == null || productosIds.isEmpty()) {
            return new ArrayList<>(); // Retorna lista vacía si no hay IDs
        }

        // Realizar la consulta con los IDs de productos
        return em.createQuery("SELECT p FROM Producto p WHERE p.id IN :ids", Producto.class)
                .setParameter("ids", productosIds)
                .getResultList();
    }

    public List<Categoria> obtenerCategorias(){
        return em.createQuery("SELECT p FROM Categoria p", Categoria.class)
                .getResultList();
    }
}


