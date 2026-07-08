package ec.edu.ups.icc.fundamentos01.products.repositories;

import ec.edu.ups.icc.fundamentos01.products.entities.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    Optional<ProductEntity> findByNameIgnoreCaseAndDeletedFalse(String name);
    List<ProductEntity> findByDeletedFalse();
    Optional<ProductEntity> findByIdAndDeletedFalse(Long id);
    List<ProductEntity> findByOwner_IdAndDeletedFalse(Long ownerId);

    @Query("""
            SELECT p FROM ProductEntity p
            WHERE p.deleted = false
              AND p.owner.id = :userId
              AND p.owner.deleted = false
              AND (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', COALESCE(:name, ''), '%')))
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            """)
    List<ProductEntity> findByOwnerIdWithFilters(
            @Param("userId") Long userId,
            @Param("name") String name,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice
    );

    @Query("""
            SELECT DISTINCT p FROM ProductEntity p
            JOIN p.categories c
            WHERE p.deleted = false
              AND c.id = :categoryId
              AND c.deleted = false
              AND p.owner.deleted = false
              AND (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', COALESCE(:name, ''), '%')))
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:userId IS NULL OR p.owner.id = :userId)
            """)
    List<ProductEntity> findByCategoryIdWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("name") String name,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("userId") Long userId
    );

    @Query(
            value = "SELECT p FROM ProductEntity p WHERE p.deleted = false",
            countQuery = "SELECT COUNT(p) FROM ProductEntity p WHERE p.deleted = false"
    )
    Page<ProductEntity> findActivePage(Pageable pageable);

    @Query("SELECT p FROM ProductEntity p WHERE p.deleted = false")
    Slice<ProductEntity> findActiveSlice(Pageable pageable);

    @Query(
            value = """
                    SELECT DISTINCT p FROM ProductEntity p
                    JOIN p.categories c
                    WHERE p.deleted = false
                      AND c.id = :categoryId
                      AND c.deleted = false
                      AND p.owner.deleted = false
                      AND (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', COALESCE(:name, ''), '%')))
                      AND (:minPrice IS NULL OR p.price >= :minPrice)
                      AND (:maxPrice IS NULL OR p.price <= :maxPrice)
                      AND (:userId IS NULL OR p.owner.id = :userId)
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT p) FROM ProductEntity p
                    JOIN p.categories c
                    WHERE p.deleted = false
                      AND c.id = :categoryId
                      AND c.deleted = false
                      AND p.owner.deleted = false
                      AND (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', COALESCE(:name, ''), '%')))
                      AND (:minPrice IS NULL OR p.price >= :minPrice)
                      AND (:maxPrice IS NULL OR p.price <= :maxPrice)
                      AND (:userId IS NULL OR p.owner.id = :userId)
                    """
    )
    Page<ProductEntity> findByCategoryIdWithFiltersPage(
            @Param("categoryId") Long categoryId,
            @Param("name") String name,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT p FROM ProductEntity p
            JOIN p.categories c
            WHERE p.deleted = false
              AND c.id = :categoryId
              AND c.deleted = false
              AND p.owner.deleted = false
              AND (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', COALESCE(:name, ''), '%')))
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:userId IS NULL OR p.owner.id = :userId)
            """)
    Slice<ProductEntity> findByCategoryIdWithFiltersSlice(
            @Param("categoryId") Long categoryId,
            @Param("name") String name,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("userId") Long userId,
            Pageable pageable
    );
}