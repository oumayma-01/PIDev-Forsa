import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ProductComparisonService } from '../../shared/services/product-comparison.service';
import { InsuranceProductComparisonDTO } from '../../shared/models/insurance.models';
import { FormsModule } from '@angular/forms';
import { ForsaIconComponent } from '../../../../shared/ui/forsa-icon/forsa-icon.component';

@Component({
  selector: 'app-client-product-catalog',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ForsaIconComponent],
  templateUrl: './client-product-catalog.component.html',
  styleUrls: ['./client-product-catalog.component.css']
})
export class ClientProductCatalogComponent implements OnInit {
  private readonly productComparisonService = inject(ProductComparisonService);
  private readonly router = inject(Router);

  products: InsuranceProductComparisonDTO[] = [];
  selectedForComparison: Set<number> = new Set();
  
  isLoading = true;
  comparisonResult: any = null;

  // Filters
  searchTerm = '';
  selectedType = '';
  minPrice: number | null = null;
  maxPrice: number | null = null;

  get filteredProducts() {
    return this.products.filter(p => {
      const matchesSearch = p.productName?.toLowerCase().includes(this.searchTerm.toLowerCase()) || 
                           p.description?.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesType = !this.selectedType || p.policyType === this.selectedType;
      const matchesMin = this.minPrice === null || (p.premiumAmount ?? 0) >= this.minPrice;
      const matchesMax = this.maxPrice === null || (p.premiumAmount ?? 0) <= this.maxPrice;
      
      return matchesSearch && matchesType && matchesMin && matchesMax;
    });
  }

  get productTypes() {
    return Array.from(new Set(this.products.map(p => p.policyType).filter(Boolean)));
  }

  ngOnInit() {
    this.loadProducts();
  }

  loadProducts() {
    this.productComparisonService.getAllProductsForComparison().subscribe({
      next: (data) => {
        this.products = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load products', err);
        this.isLoading = false;
      }
    });
  }

  toggleComparison(productId?: number) {
    if (!productId) return;
    if (this.selectedForComparison.has(productId)) {
      this.selectedForComparison.delete(productId);
    } else {
      if (this.selectedForComparison.size < 3) {
        this.selectedForComparison.add(productId);
      } else {
        alert('You can compare up to 3 products at once.');
      }
    }
  }

  isSelected(productId?: number): boolean {
    return productId ? this.selectedForComparison.has(productId) : false;
  }

  compareSelected() {
    if (this.selectedForComparison.size < 2) {
      alert('Please select at least 2 products to compare.');
      return;
    }
    const ids = Array.from(this.selectedForComparison);
    this.productComparisonService.compareProducts(ids).subscribe({
      next: (res) => {
        this.comparisonResult = res;
      },
      error: (err) => {
        console.error('Comparison failed', err);
      }
    });
  }

  applyForProduct(productId?: number) {
    if(productId) {
      this.router.navigate(['/dashboard/insurance/client/apply', productId]);
    }
  }

  clearComparison() {
    this.selectedForComparison.clear();
    this.comparisonResult = null;
  }
}
