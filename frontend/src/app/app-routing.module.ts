import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ProductDetailComponent } from './product-detail/product-detail.component';
import { ProductFormComponent } from './product-form/product-form.component';

const routes: Routes = [
  { path: '', redirectTo: 'products', pathMatch: 'full' },
  { path: 'products', component: DashboardComponent },
  { path: 'products/new', component: ProductFormComponent },
  { path: 'products/:id', component: ProductDetailComponent },
  { path: 'products/:id/edit', component: ProductFormComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
