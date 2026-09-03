import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { ProductFormComponent } from './product-form.component';
import { ProductService } from '../services/product.service';

describe('ProductFormComponent', () => {
  let component: ProductFormComponent;
  let fixture: ComponentFixture<ProductFormComponent>;
  let productServiceSpy: jasmine.SpyObj<ProductService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    productServiceSpy = jasmine.createSpyObj('ProductService', ['create', 'update', 'getById']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [ProductFormComponent],
      imports: [CommonModule, ReactiveFormsModule],
      providers: [
        { provide: ProductService, useValue: productServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => null } } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create in "create" mode when no id in route', () => {
    expect(component.isEditMode).toBeFalse();
  });

  it('should mark form invalid when name is blank', () => {
    component.form.patchValue({ name: '', price: 10, quantity: 1 });
    expect(component.form.invalid).toBeTrue();
  });

  it('should mark form invalid when price is negative', () => {
    component.form.patchValue({ name: 'Valid', price: -5, quantity: 1 });
    expect(component.form.invalid).toBeTrue();
  });

  it('should mark form valid with correct values', () => {
    component.form.patchValue({ name: 'Valid', description: '', price: 10, quantity: 1 });
    expect(component.form.valid).toBeTrue();
  });

  it('should call create() and navigate on successful submit in create mode', () => {
    productServiceSpy.create.and.returnValue(of({
      id: 5, name: 'Valid', description: '', price: 10, quantity: 1,
      createdDate: '', updatedDate: ''
    }));
    component.form.patchValue({ name: 'Valid', description: '', price: 10, quantity: 1 });

    component.onSubmit();

    expect(productServiceSpy.create).toHaveBeenCalled();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/products', 5]);
  });

  it('should not submit when form is invalid', () => {
    component.form.patchValue({ name: '', price: 10, quantity: 1 });

    component.onSubmit();

    expect(productServiceSpy.create).not.toHaveBeenCalled();
  });
});
