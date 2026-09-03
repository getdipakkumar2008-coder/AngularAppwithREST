import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Product } from '../models/product.model';
import { ProductService } from '../services/product.service';

@Component({
  selector: 'app-product-detail',
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.css']
})
export class ProductDetailComponent implements OnInit {
  product: Product | null = null;
  loading = false;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      this.error = 'Invalid product id.';
      return;
    }
    this.loadProduct(Number(idParam));
  }

  private loadProduct(id: number): void {
    this.loading = true;
    this.error = null;
    this.productService.getById(id).subscribe({
      next: (product) => {
        this.product = product;
        this.loading = false;
      },
      error: () => {
        this.error = 'Product not found.';
        this.loading = false;
      }
    });
  }

  editProduct(): void {
    if (this.product) {
      this.router.navigate(['/products', this.product.id, 'edit']);
    }
  }

  deleteProduct(): void {
    if (!this.product) {
      return;
    }
    if (!confirm(`Delete "${this.product.name}"? This cannot be undone.`)) {
      return;
    }
    this.productService.delete(this.product.id).subscribe({
      next: () => this.router.navigate(['/products']),
      error: () => {
        this.error = 'Failed to delete product. Please try again.';
      }
    });
  }

  backToDashboard(): void {
    this.router.navigate(['/products']);
  }
}
