import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ProductService } from './product.service';
import { Product } from '../models/product.model';

describe('ProductService', () => {
  let service: ProductService;
  let httpMock: HttpTestingController;
  const apiUrl = '/api/products';

  const sampleProduct: Product = {
    id: 1,
    name: 'Sample Product',
    description: 'A sample',
    price: 19.99,
    quantity: 5,
    createdDate: '2026-09-03T10:00:00Z',
    updatedDate: '2026-09-03T10:00:00Z'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ProductService]
    });
    service = TestBed.inject(ProductService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getAll() should GET the products list', () => {
    service.getAll().subscribe(products => {
      expect(products).toEqual([sampleProduct]);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush([sampleProduct]);
  });

  it('getById() should GET a single product', () => {
    service.getById(1).subscribe(product => {
      expect(product).toEqual(sampleProduct);
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(sampleProduct);
  });

  it('create() should POST a new product', () => {
    const newProduct = { name: 'New', description: '', price: 5, quantity: 1 };

    service.create(newProduct).subscribe(product => {
      expect(product).toEqual(sampleProduct);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newProduct);
    req.flush(sampleProduct);
  });

  it('update() should PUT to the product endpoint', () => {
    const updated = { name: 'Updated', description: '', price: 10, quantity: 2 };

    service.update(1, updated).subscribe(product => {
      expect(product).toEqual(sampleProduct);
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('PUT');
    req.flush(sampleProduct);
  });

  it('delete() should DELETE the product', () => {
    service.delete(1).subscribe(response => {
      expect(response).toBeNull();
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('getAll() should propagate errors', () => {
    service.getAll().subscribe({
      next: () => fail('expected an error'),
      error: (err) => expect(err.status).toBe(500)
    });

    const req = httpMock.expectOne(apiUrl);
    req.flush('server error', { status: 500, statusText: 'Internal Server Error' });
  });
});
