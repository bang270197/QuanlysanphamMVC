package com.codegym.controller;

import com.codegym.model.Product;
import com.codegym.service.ProductService;
import com.codegym.service.ProductServiceImpl;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10, // 10MB
        maxRequestSize = 1024 * 1024 * 50) // 50MB
@WebServlet(name = "ProductServlet", urlPatterns = "/products")
public class ProductServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public static final String SAVE_DIRECTORY = "images";

    public ProductServlet() {
        super();
    }
    private ProductService productService = new ProductServiceImpl();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }
        switch (action) {
            case "create":
                createProduct(request, response);
                break;
            case "edit":
                updateProduct(request, response);
                break;
            case "delete":
               deleteProduct(request,response);
                break;
          case "search":
                showFormSearch(request,response);
                break;
            default:
                break;
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }
        switch (action) {
            case "create":
                showFormCreate(request, response);
                break;
            case "edit":
                showFormEdit(request, response);
                break;
            case "delete":
                showFormDelete(request,response);
                break;
            case "view":
                View(request,response);
                break;
            default:
                listProducts(request, response);
                break;
        }
    }

    private void listProducts(HttpServletRequest request, HttpServletResponse response) {
        List<Product> products = this.productService.findAll();
        //dispatcher dieu huong sang trang khac
        RequestDispatcher dispatcher = request.getRequestDispatcher("viewProduct/list.jsp");
        request.setAttribute("products", products);

        try {
            dispatcher.forward(request, response);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showFormCreate(HttpServletRequest request, HttpServletResponse response) {
        RequestDispatcher dispatcher = request.getRequestDispatcher("viewProduct/create.jsp");
        try {
            dispatcher.forward(request, response);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createProduct(HttpServletRequest request, HttpServletResponse response) {
        String name = request.getParameter("name");
        float price = Float.parseFloat(request.getParameter("price"));
        String description = request.getParameter("description");
        String producer = request.getParameter("producer");
        int id = (int) (Math.random() * 100);

        String avatar = "";
        String fileName = null;

        try {

            // Đường dẫn tuyệt đối tới thư mục gốc của web app.
            String appPath = request.getServletContext().getRealPath("");
            appPath = appPath.replace('\\', '/');


            // Thư mục để save file tải lên.
            String fullSavePath = null;
            if (appPath.endsWith("/")) {
                fullSavePath = appPath+SAVE_DIRECTORY;
            } else {
                fullSavePath = appPath+ "/" + SAVE_DIRECTORY;
            }

            // Tạo thư mục nếu nó không tồn tại.
            File fileSaveDir = new File(fullSavePath);
            if (!fileSaveDir.exists()) {
                fileSaveDir.mkdir();
            }

            // Danh mục các phần đã upload lên (Có thể là nhiều file).
            for (Part part : request.getParts()) {
                fileName = extractFileName(part);
                if (fileName != null && fileName.length() > 0) {
                    String filePath = fullSavePath + File.separator + fileName;
                    System.out.println("Write attachment to file: " + filePath);

                    // Ghi vào file.
                    part.write(filePath);
                    avatar = fileName;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Product product = new Product(id, name, price, description, producer,);
        this.productService.save(product);
        RequestDispatcher dispatcher = request.getRequestDispatcher("viewProduct/create.jsp");
        request.setAttribute("message", "Them san pham thanh cong");
        try {
            dispatcher.forward(request, response);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                String clientFileName = s.substring(s.indexOf("=") + 2, s.length() - 1);
                clientFileName = clientFileName.replace("\\", "/");
                int i = clientFileName.lastIndexOf('/');
                return clientFileName.substring(i + 1);
            }
        }
        return null;
    }



    private void showFormEdit(HttpServletRequest request, HttpServletResponse response) {
        int id = Integer.parseInt(request.getParameter("id"));
        Product product = this.productService.findById(id);
        RequestDispatcher dispatcher;
        if (product == null) {
            dispatcher = request.getRequestDispatcher("error-404.jsp");
        } else {
            request.setAttribute("product", product);
            dispatcher = request.getRequestDispatcher("viewProduct/edit.jsp");
        }
        try {
            dispatcher.forward(request, response);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateProduct(HttpServletRequest request, HttpServletResponse response) {
        int id = Integer.parseInt(request.getParameter("id"));
        String name = request.getParameter("name");
        Float price = Float.parseFloat(request.getParameter("price"));
        String description = request.getParameter("description");
        String producer = request.getParameter("producer");

        String fileName = null;
        String avatar = "";
        try {

            // Đường dẫn tuyệt đối tới thư mục gốc của web app.
            String appPath = request.getServletContext().getRealPath("");
            appPath = appPath.replace('\\', '/');


            // Thư mục để save file tải lên.
            String fullSavePath = null;
            if (appPath.endsWith("/")) {
                fullSavePath = appPath+SAVE_DIRECTORY;
            } else {
                fullSavePath = appPath+ "/" + SAVE_DIRECTORY;
            }


            // Tạo thư mục nếu nó không tồn tại.
            File fileSaveDir = new File(fullSavePath);
            if (!fileSaveDir.exists()) {
                fileSaveDir.mkdir();
            }

            // Danh mục các phần đã upload lên (Có thể là nhiều file).
            for (Part part : request.getParts()) {
                fileName = extractFileName(part);
                if (fileName != null && fileName.length() > 0) {
                    String filePath = fullSavePath + File.separator + fileName;
                    System.out.println("Write attachment to file: " + filePath);

                    // Ghi vào file.
                    part.write(filePath);
                    avatar = fileName;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Product product = this.productService.findById(id);
        RequestDispatcher dispatcher;
        if (product == null) {
            dispatcher = request.getRequestDispatcher("error-404.jsp");
        } else {
            product.setName(name);
            product.setPrice(price);
            product.setDescription(description);
            product.setProducer(producer);
            product.setAvatar(avatar);
            this.productService.edit(id, product);
            request.setAttribute("product", product);
            request.setAttribute("message", "Sua thanh cong");
            dispatcher = request.getRequestDispatcher("viewProduct/edit.jsp");
        }
        try {
            dispatcher.forward(request, response);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void showFormDelete(HttpServletRequest request, HttpServletResponse response) {
        int id=Integer.parseInt(request.getParameter("id"));
        Product product=this.productService.findById(id);
        RequestDispatcher dispatcher;
        if (product==null){
            dispatcher=request.getRequestDispatcher("error-404.jsp");
        }else {
            request.setAttribute("product",product);
            dispatcher=request.getRequestDispatcher("viewProduct/delete.jsp");
        }
        try {
            dispatcher.forward(request, response);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void deleteProduct(HttpServletRequest request, HttpServletResponse response){
        int id=Integer.parseInt(request.getParameter("id"));
        Product product=this.productService.findById(id);
        RequestDispatcher dispatcher;
        if (product==null){
            dispatcher=request.getRequestDispatcher("error-404.jsp");
        }else {
            this.productService.delete(id);
            request.setAttribute("product",product);
            request.setAttribute("message","Xoa thanh cong");
            dispatcher=request.getRequestDispatcher("viewProduct/delete.jsp");
        }
        try {
            dispatcher.forward(request, response);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void View(HttpServletRequest request, HttpServletResponse response){
        int id=Integer.parseInt(request.getParameter("id"));
        Product product=this.productService.findById(id);
        RequestDispatcher dispatcher;
        if (product == null){
            dispatcher=request.getRequestDispatcher("error-404.jsp");
        }else {
            request.setAttribute("product",product);
            dispatcher=request.getRequestDispatcher("viewProduct/view.jsp");
        }
        try {
            dispatcher.forward(request, response);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showFormSearch(HttpServletRequest request, HttpServletResponse response){
        String name=request.getParameter("searchname");
        ArrayList<Product> products=this.productService.findByName(name);
        RequestDispatcher dispatcher ;
        if (products == null){
            dispatcher=request.getRequestDispatcher("error-404.jsp");
        }else {
            request.setAttribute("products",products);
            dispatcher=request.getRequestDispatcher("viewProduct/search.jsp");
        }
        try {
            dispatcher.forward(request, response);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
