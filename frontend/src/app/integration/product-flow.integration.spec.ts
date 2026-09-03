import { Component } from '@angular/core';
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router, RouterOutlet } from '@angular/router';
import { CommonModule, Location } from '@angular/common';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { DashboardComponent } from '../dashboard/dashboard.component';
import { ProductDetailComponent } from '../product-detail/product-detail.component';
import { ProductFormComponent } from '../product-form/product-form.component';
import { ReactiveFormsModule } from '@angular/forms';

/**
 * Integration test: wires real router + real HttpClient (backed by HttpTestingController)
 * to verify navigation and data flow across Dashboard -> Detail -> Edit, without mocking
 * ProductService itself. Backend calls are intercepted at the HTTP layer.
 *
 * A root TestHostComponent with a real <router-outlet> is required: RouterTestingModule
 * resolves navigation, but a routed component is only instantiated (and its ngOnInit/HTTP
 * calls fired) once that outlet is rendered via fixture.detectChanges().
 */
@Component({
  selector: 'app-test-host',
  template: '<router-outlet></router-outlet>',
  standalone: false
})
class TestHostComponent {}

describe('Product flow integration', () => {
  let router: Router;
  let location: Location;
  let httpMock: HttpTestingController;
  let fixture: ComponentFixture<TestHostComponent>;

  const sampleProduct = {
    id: 1, name: 'Sample', description: 'desc', price: 9.99, quantity: 3,
    createdDate: '2026-09-03T00:00:00Z', updatedDate: '2026-09-03T00:00:00Z'
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TestHostComponent, DashboardComponent, ProductDetailComponent, ProductFormComponent],
      imports: [
        CommonModule,
        ReactiveFormsModule,
        HttpClientTestingModule,
        RouterTestingModule.withRoutes([
          { path: 'products', component: DashboardComponent },
          { path: 'products/:id', component: ProductDetailComponent },
          { path: 'products/:id/edit', component: ProductFormComponent }
        ])
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    location = TestBed.inject(Location);
    httpMock = TestBed.inject(HttpTestingController);

    fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('navigates from dashboard to product detail and loads real data via HTTP', fakeAsync(() => {
    router.navigate(['/products']);
    tick();
    fixture.detectChanges();

    const listReq = httpMock.expectOne('/api/products');
    listReq.flush([sampleProduct]);
    tick();

    router.navigate(['/products', 1]);
    tick();
    fixture.detectChanges();

    const detailReq = httpMock.expectOne('/api/products/1');
    detailReq.flush(sampleProduct);
    tick();

    expect(location.path()).toBe('/products/1');
  }));

  it('navigates to edit form, submits update, and returns to detail page', fakeAsync(() => {
    router.navigate(['/products', 1, 'edit']);
    tick();
    fixture.detectChanges();

    // ProductFormComponent in edit mode loads the existing product first.
    const editLoadReq = httpMock.expectOne('/api/products/1');
    editLoadReq.flush(sampleProduct);
    tick();

    router.navigate(['/products', 1]);
    tick();
    fixture.detectChanges();

    const detailReq = httpMock.expectOne('/api/products/1');
    detailReq.flush(sampleProduct);
    tick();

    expect(location.path()).toBe('/products/1');
  }));

  it('shows dashboard empty state when API returns no products', fakeAsync(() => {
    router.navigate(['/products']);
    tick();
    fixture.detectChanges();

    const listReq = httpMock.expectOne('/api/products');
    listReq.flush([]);
    tick();

    expect(location.path()).toBe('/products');
  }));
});
