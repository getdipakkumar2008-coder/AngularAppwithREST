import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../services/product.service';

@Component({
  selector: 'app-product-form',
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.css']
})
export class ProductFormComponent implements OnInit {
  form: FormGroup;
  isEditMode = false;
  productId: number | null = null;
  submitting = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      description: ['', [Validators.maxLength(1000)]],
      price: [0, [Validators.required, Validators.min(0)]],
      quantity: [0, [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode = true;
      this.productId = Number(idParam);
      this.loadProduct(this.productId);
    }
  }

  private loadProduct(id: number): void {
    this.productService.getById(id).subscribe({
      next: (product) => {
        this.form.patchValue({
          name: product.name,
          description: product.description,
          price: product.price,
          quantity: product.quantity
        });
      },
      error: () => {
        this.error = 'Failed to load product.';
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.error = null;
    const payload = this.form.value;

    const request = this.isEditMode && this.productId !== null
      ? this.productService.update(this.productId, payload)
      : this.productService.create(payload);

    request.subscribe({
      next: (product) => {
        this.submitting = false;
        this.router.navigate(['/products', product.id]);
      },
      error: () => {
        this.submitting = false;
        this.error = 'Failed to save product. Please check the form and try again.';
      }
    });
  }

  cancel(): void {
    if (this.isEditMode && this.productId !== null) {
      this.router.navigate(['/products', this.productId]);
    } else {
      this.router.navigate(['/products']);
    }
  }
}
