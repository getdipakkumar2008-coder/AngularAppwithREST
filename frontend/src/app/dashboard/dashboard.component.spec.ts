import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { DashboardComponent } from './dashboard.component';
import { ProductService } from '../services/product.service';
import { Product } from '../models/product.model';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let productServiceSpy: jasmine.SpyObj<ProductService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const products: Product[] = [
    { id: 1, name: 'A', description: '', price: 1, quantity: 1, createdDate: '', updatedDate: '' },
    { id: 2, name: 'B', description: '', price: 2, quantity: 2, createdDate: '', updatedDate: '' }
  ];

  beforeEach(async () => {
    productServiceSpy = jasmine.createSpyObj('ProductService', ['getAll', 'delete']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [CommonModule],
      declarations: [DashboardComponent],
      providers: [
        { provide: ProductService, useValue: productServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    productServiceSpy.getAll.and.returnValue(of([]));
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should load products on init', () => {
    productServiceSpy.getAll.and.returnValue(of(products));

    fixture.detectChanges();

    expect(component.products).toEqual(products);
    expect(component.loading).toBeFalse();
    expect(component.error).toBeNull();
  });

  it('should set error state when load fails', () => {
    productServiceSpy.getAll.and.returnValue(throwError(() => new Error('network error')));

    fixture.detectChanges();

    expect(component.error).toBeTruthy();
    expect(component.loading).toBeFalse();
  });

  it('should show empty state when list is empty', () => {
    productServiceSpy.getAll.and.returnValue(of([]));

    fixture.detectChanges();

    expect(component.products.length).toBe(0);
  });

  it('should navigate to detail page when a product is selected', () => {
    productServiceSpy.getAll.and.returnValue(of(products));
    fixture.detectChanges();

    component.viewProduct(products[0]);

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/products', 1]);
  });

  it('should call delete service and remove item from list on confirm', () => {
    productServiceSpy.getAll.and.returnValue(of(products));
    productServiceSpy.delete.and.returnValue(of(void 0));
    fixture.detectChanges();

    component.deleteProduct(products[0]);

    expect(productServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(component.products.find(p => p.id === 1)).toBeUndefined();
  });
});
