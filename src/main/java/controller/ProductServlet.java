package main.java.controller;

import main.java.model.Catalog;
import main.java.model.ImportRecord;
import main.java.model.Product;
import main.java.service.catalog.CatalogService;
import main.java.service.catalog.ICatalogService;
import main.java.service.product.IProductService;
import main.java.service.product.ProductServiceImp;
import main.java.service.stock.IStockService;
import main.java.service.stock.StockServiceImp;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * @author Duc on 5/17/2020
 * @project casestudy-module3-duongshoe
 **/

@WebServlet(name = "ServletProduct", urlPatterns = "/product")
public class ProductServlet extends HttpServlet {
    private IProductService productService = new ProductServiceImp();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }
        switch (action) {
            case "register":
                addProduct(request, response);
                break;
        }
    }

    private void addProduct(HttpServletRequest request, HttpServletResponse response) {
        Product product = new Product();
        product.setProductName(request.getParameter("product-name"));
        product.setCatalogID(Integer.parseInt(request.getParameter("catalog-id")));
        product.setSize(Integer.parseInt(request.getParameter("product-size")));
        product.setDescription(request.getParameter("product-description"));
        product.addImages(request.getParameter("image-link-1"));
        product.addImages(request.getParameter("image-link-2"));
        product.addImages(request.getParameter("image-link-3"));
        try {
            if (productService.addToDB(product)) {
                request.setAttribute("message", "Thêm Thành công");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        showRegisterForm(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }
        switch (action) {
            case "detail":
                displayDetail(request, response);
                break;
            case "register":
                showRegisterForm(request, response);
                break;
            default:
                displayProductList(request, response);
        }
    }

    private void showRegisterForm(HttpServletRequest request, HttpServletResponse response) {
        ICatalogService catalogService = new CatalogService();
        try {
            List<Catalog> catalogList = catalogService.getCatalogList();
            List<Integer> sizeList = productService.getSizeList();
            request.setAttribute("catalogList", catalogList);
            request.setAttribute("sizeList", sizeList);
            request.getRequestDispatcher("views/admin/product/register.jsp").forward(request, response);
        } catch (ServletException | IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayDetail(HttpServletRequest request, HttpServletResponse response) {
        int id = Integer.parseInt(request.getParameter("id"));
        IStockService stockService = new StockServiceImp();
        String requestDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        try {
            List<ImportRecord> importRecords = stockService.getImportRecordByProductID(id);
            importRecords.sort(new Comparator<ImportRecord>() {
                @Override
                public int compare(ImportRecord o1, ImportRecord o2) {
                    return o1.getImportDateTime().compareTo(o2.getImportDateTime());
                }
            });
            request.setAttribute("importRecords", importRecords);
            request.setAttribute("requestDate", requestDate);
            request.getRequestDispatcher("views/admin/product/detail.jsp").forward(request, response);
        } catch (ServletException | IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayProductList(HttpServletRequest request, HttpServletResponse response) {
        try {
            List<Product> products = productService.getProductList();
            request.setAttribute("products", products);
            request.getRequestDispatcher("views/admin/product/home.jsp").forward(request, response);
        } catch (ServletException | IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
