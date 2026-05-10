from django.http import Http404

from .models import Product
from .dto import ProductRequestDTO, ProductResponseDTO
from .middleware import ValidationError

class ProductService:
    def __init__(self, product_repository=None):
        self.product_repository = product_repository or Product.objects

    def list_products(self):
        products = self.product_repository.all()
        return [ProductResponseDTO(p).to_dict() for p in products]

    def get_product(self, product_id):
        try:
            product = self.product_repository.get(id=product_id)
        except Product.DoesNotExist:
            raise Http404(f"Product with id {product_id} not found")
        return ProductResponseDTO(product).to_dict()

    def create_product(self, data: dict):
        dto = ProductRequestDTO(data)
        errors = dto.validate()
        if errors:
            raise ValidationError(errors)

        product = self.product_repository.create(
            name=dto.name,
            price=dto.price,
            description=dto.description,
        )
        return ProductResponseDTO(product).to_dict()

    def delete_product(self, product_id):
        try:
            product = self.product_repository.get(id=product_id)
        except Product.DoesNotExist:
            raise Http404(f"Product with id {product_id} not found")
        product.delete()
        return {'message': 'Record deleted successfully'}
