import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Product } from '../models/product.model';
import { ProductService } from '../services/product.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  products: Product[] = [];
  loading = false;
  error: string | null = null;

  constructor(private productService: ProductService, private router: Router) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.error = null;
    this.productService.getAll().subscribe({
      next: (products) => {
        this.products = products;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load products. Please try again.';
        this.loading = false;
      }
    });
  }

  viewProduct(product: Product): void {
    this.router.navigate(['/products', product.id]);
  }

  goToCreate(): void {
    this.router.navigate(['/products/new']);
  }

  onDeleteClick(product: Product): void {
    if (!confirm(`Delete "${product.name}"? This cannot be undone.`)) {
      return;
    }
    this.deleteProduct(product);
  }

  deleteProduct(product: Product): void {
    this.productService.delete(product.id).subscribe({
      next: () => {
        this.products = this.products.filter((p) => p.id !== product.id);
      },
      error: () => {
        this.error = 'Failed to delete product. Please try again.';
      }
    });
  }
}
