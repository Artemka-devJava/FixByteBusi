package ru.fixbyte.model;

public class ReceiptItem {
    private final Product product;
    private double quantity;
    private double total;

    public ReceiptItem(Product product, double quantity) {
        this.product = product;
        this.quantity = quantity;
        this.total = product.getPrice() * quantity;
    }

    public Product getProduct() { return product; }
    public double getQuantity() { return quantity; }
    public double getTotal() { return total; }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
        this.total = product. getPrice() * quantity;
    }
}
