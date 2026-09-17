-- ============================================================
-- 1. Tạo database (bỏ qua nếu đã tồn tại)
-- ============================================================
IF NOT EXISTS (
    SELECT name FROM sys.databases WHERE name = N'webst3'
)
BEGIN
    CREATE DATABASE webst3 COLLATE Vietnamese_CI_AS;
    PRINT N'Database webst3 đã được tạo.';
END
ELSE
BEGIN
    PRINT N'Database webst3 đã tồn tại, bỏ qua bước tạo.';
END
GO

USE webst3;
GO

-- ============================================================
-- 2. Tạo bảng categories
-- ============================================================
IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'categories'
)
BEGIN
    CREATE TABLE categories (
        id          BIGINT          NOT NULL IDENTITY(1,1),
        name        NVARCHAR(200)   NOT NULL,
        description NVARCHAR(500)   NULL,
        icon        NVARCHAR(500)   NULL,
        created_at  DATETIME2       NULL DEFAULT GETDATE(),
        CONSTRAINT PK_categories PRIMARY KEY (id),
        CONSTRAINT UQ_categories_name UNIQUE (name)
    );
    PRINT N'Bảng categories đã được tạo.';
END
ELSE
BEGIN
    PRINT N'Bảng categories đã tồn tại, bỏ qua.';
END
GO

-- ============================================================
-- 3. Tạo bảng products (có category_id)
-- ============================================================
IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'products'
)
BEGIN
    CREATE TABLE products (
        id          BIGINT          NOT NULL IDENTITY(1,1),
        name        NVARCHAR(200)   NOT NULL,
        price       DECIMAL(18, 2)  NOT NULL    DEFAULT 0,
        quantity    INT             NOT NULL    DEFAULT 0,
        description NVARCHAR(1000)  NULL,
        category_id BIGINT          NULL,
        created_at  DATETIME2       NULL        DEFAULT GETDATE(),
        CONSTRAINT PK_products           PRIMARY KEY (id),
        CONSTRAINT CHK_products_price    CHECK (price >= 0),
        CONSTRAINT CHK_products_quantity CHECK (quantity >= 0),
        CONSTRAINT FK_products_category
            FOREIGN KEY (category_id) REFERENCES categories(id)
            ON DELETE SET NULL
    );
    PRINT N'Bảng products đã được tạo (có category_id).';
END
ELSE
BEGIN
    PRINT N'Bảng products đã tồn tại, kiểm tra cột images và category_id...';

    IF EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME = 'products' AND COLUMN_NAME = 'images'
    )
    BEGIN
        ALTER TABLE products DROP COLUMN images;
        PRINT N'  → Đã xóa cột images cũ.';
    END

    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME = 'products' AND COLUMN_NAME = 'category_id'
    )
    BEGIN
        ALTER TABLE products ADD category_id BIGINT NULL;
        ALTER TABLE products
            ADD CONSTRAINT FK_products_category
            FOREIGN KEY (category_id) REFERENCES categories(id)
            ON DELETE SET NULL;
        PRINT N'  → Đã thêm cột category_id và FK.';
    END
    ELSE
    BEGIN
        PRINT N'  → Cột category_id đã tồn tại, bỏ qua.';
    END
END
GO

-- ============================================================
-- 4. Tạo bảng product_images
-- ============================================================
IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'product_images'
)
BEGIN
    CREATE TABLE product_images (
        id            BIGINT          NOT NULL IDENTITY(1,1),
        product_id    BIGINT          NOT NULL,
        image_url     NVARCHAR(500)   NOT NULL,
        is_primary    BIT             NOT NULL DEFAULT 0,
        display_order INT             NOT NULL DEFAULT 0,
        created_at    DATETIME2       NULL     DEFAULT GETDATE(),
        CONSTRAINT PK_product_images PRIMARY KEY (id),
        CONSTRAINT FK_product_images_products
            FOREIGN KEY (product_id) REFERENCES products(id)
            ON DELETE CASCADE
    );
    PRINT N'Bảng product_images đã được tạo.';
END
ELSE
BEGIN
    PRINT N'Bảng product_images đã tồn tại, bỏ qua.';
END
GO

-- ============================================================
-- 5. Dữ liệu mẫu categories
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM categories)
BEGIN
    INSERT INTO categories (name, description, icon, created_at) VALUES
    (N'Laptop',         N'Máy tính xách tay các loại',          NULL, GETDATE()),
    (N'Màn hình',       N'Màn hình máy tính, gaming monitor',   NULL, GETDATE()),
    (N'Phụ kiện',       N'Bàn phím, chuột, tai nghe, webcam',   NULL, GETDATE()),
    (N'Điện thoại',     N'Điện thoại thông minh các hãng',      NULL, GETDATE()),
    (N'Máy tính bảng',  N'iPad, Android tablet',                NULL, GETDATE()),
    (N'Lưu trữ',        N'SSD, HDD, USB, thẻ nhớ',              NULL, GETDATE());
    PRINT N'Đã chèn 6 danh mục mẫu.';
END
GO

-- ============================================================
-- 6. Dữ liệu mẫu products
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM products)
BEGIN
    DECLARE @cLaptop        BIGINT = (SELECT id FROM categories WHERE name = N'Laptop');
    DECLARE @cManHinh       BIGINT = (SELECT id FROM categories WHERE name = N'Màn hình');
    DECLARE @cPhuKien       BIGINT = (SELECT id FROM categories WHERE name = N'Phụ kiện');
    DECLARE @cDienThoai     BIGINT = (SELECT id FROM categories WHERE name = N'Điện thoại');
    DECLARE @cMayTinhBang   BIGINT = (SELECT id FROM categories WHERE name = N'Máy tính bảng');
    DECLARE @cLuuTru        BIGINT = (SELECT id FROM categories WHERE name = N'Lưu trữ');

    INSERT INTO products (name, price, quantity, description, category_id, created_at) VALUES
    (N'Laptop Dell Inspiron 15',       15990000, 25, N'Laptop Dell Inspiron 15, Core i5, RAM 8GB, SSD 256GB',          @cLaptop,      GETDATE()),
    (N'Laptop HP Pavilion 14',         13500000, 30, N'Laptop HP Pavilion 14, Core i5, RAM 8GB, SSD 512GB',            @cLaptop,      GETDATE()),
    (N'Màn hình Samsung 24 inch',       4500000, 50, N'Màn hình Samsung Full HD, 75Hz, IPS, viền mỏng',                @cManHinh,     GETDATE()),
    (N'Bàn phím cơ Keychron K2',        1890000, 80, N'Bàn phím cơ Keychron K2, switch Brown, có đèn RGB',             @cPhuKien,     GETDATE()),
    (N'Chuột Logitech MX Master 3',     1650000, 60, N'Chuột không dây Logitech MX Master 3, sạc USB-C',               @cPhuKien,     GETDATE()),
    (N'Tai nghe Sony WH-1000XM5',       8200000, 20, N'Tai nghe chống ồn Sony WH-1000XM5, Bluetooth 5.2',              @cPhuKien,     GETDATE()),
    (N'Điện thoại Samsung Galaxy A55', 10990000, 40, N'Samsung Galaxy A55, RAM 8GB, 256GB, camera 50MP',               @cDienThoai,   GETDATE()),
    (N'iPad Air 5',                    18500000, 15, N'iPad Air 5, chip M1, 64GB, WiFi, màn hình Liquid Retina 10.9"', @cMayTinhBang, GETDATE()),
    (N'Ổ cứng SSD Kingston 1TB',        1290000,100, N'SSD Kingston A400 1TB, đọc 500MB/s, ghi 450MB/s, chuẩn SATA',  @cLuuTru,      GETDATE()),
    (N'Webcam Logitech C920',             990000, 45, N'Webcam Full HD 1080p, tích hợp micro, cắm USB',                @cPhuKien,     GETDATE());
    PRINT N'Đã chèn 10 sản phẩm mẫu.';
END
GO

-- ============================================================
-- 7. Kiểm tra kết quả
-- ============================================================
SELECT
    p.id,
    p.name          AS san_pham,
    FORMAT(p.price, 'N0') AS gia_vnd,
    p.quantity      AS so_luong,
    c.name          AS danh_muc,
    COUNT(pi.id)    AS so_anh
FROM products p
LEFT JOIN categories c      ON c.id = p.category_id
LEFT JOIN product_images pi ON pi.product_id = p.id
GROUP BY p.id, p.name, p.price, p.quantity, c.name
ORDER BY c.name, p.id;
GO

SELECT id, name, description, created_at FROM categories ORDER BY id;
GO
