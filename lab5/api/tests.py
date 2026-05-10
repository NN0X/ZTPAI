from decimal import Decimal
from unittest.mock import MagicMock

from django.http import Http404
from django.test import SimpleTestCase

from api.middleware import ValidationError
from api.models import Product
from api.services import ProductService


class ProductServiceTests(SimpleTestCase):
    def setUp(self):
        self.mock_repo = MagicMock()
        self.service = ProductService(product_repository=self.mock_repo)

    def test_create_product_happy_path_returns_dto_and_calls_repo(self):
        fake_product = MagicMock()
        fake_product.id = 1
        fake_product.name = "Laptop"
        fake_product.price = Decimal("3499.99")
        fake_product.description = "Gaming laptop"
        self.mock_repo.create.return_value = fake_product

        payload = {
            "name": "Laptop",
            "price": "3499.99",
            "description": "Gaming laptop",
        }

        result = self.service.create_product(payload)

        self.mock_repo.create.assert_called_once_with(
            name="Laptop",
            price="3499.99",
            description="Gaming laptop",
        )
        self.assertEqual(result["id"], 1)
        self.assertEqual(result["name"], "Laptop")
        self.assertEqual(result["price"], "3499.99")

    def test_list_products_returns_mapped_dtos(self):
        p1 = MagicMock(id=1, price=Decimal("99.00"))
        p1.name = "Mouse"
        p2 = MagicMock(id=2, price=Decimal("249.50"))
        p2.name = "Keyboard"
        self.mock_repo.all.return_value = [p1, p2]

        result = self.service.list_products()

        self.mock_repo.all.assert_called_once()
        self.assertEqual(len(result), 2)
        self.assertEqual(result[0]["id"], 1)
        self.assertEqual(result[0]["name"], "Mouse")
        self.assertEqual(result[1]["id"], 2)
        self.assertEqual(result[1]["price"], "249.50")

    def test_get_product_returns_dto_when_exists(self):
        fake_product = MagicMock()
        fake_product.id = 42
        fake_product.name = "Monitor"
        fake_product.price = Decimal("1299.00")
        self.mock_repo.get.return_value = fake_product

        result = self.service.get_product(product_id=42)

        self.mock_repo.get.assert_called_once_with(id=42)
        self.assertEqual(result["id"], 42)
        self.assertEqual(result["name"], "Monitor")
        self.assertEqual(result["price"], "1299.00")

    def test_get_product_raises_http404_when_not_found(self):
        self.mock_repo.get.side_effect = Product.DoesNotExist

        with self.assertRaises(Http404):
            self.service.get_product(product_id=999)

        self.mock_repo.get.assert_called_once_with(id=999)

    def test_delete_product_raises_http404_when_not_found(self):
        self.mock_repo.get.side_effect = Product.DoesNotExist

        with self.assertRaises(Http404):
            self.service.delete_product(product_id=12345)

        self.mock_repo.get.assert_called_once_with(id=12345)
        self.mock_repo.delete.assert_not_called()

    def test_create_product_with_negative_price_raises_validation_error(self):
        payload = {"name": "Hackable", "price": "-100", "description": ""}

        with self.assertRaises(ValidationError) as ctx:
            self.service.create_product(payload)

        self.assertIn("Pole 'price' musi być >= 0.", ctx.exception.errors)
        self.mock_repo.create.assert_not_called()

    def test_create_product_with_empty_name_raises_validation_error(self):
        payload = {"name": "   ", "price": "10.00", "description": ""}

        with self.assertRaises(ValidationError) as ctx:
            self.service.create_product(payload)

        self.assertTrue(
            any("name" in msg for msg in ctx.exception.errors),
            f"Spodziewałem się błędu o polu 'name', dostałem: {ctx.exception.errors}",
        )
        self.mock_repo.create.assert_not_called()

    def test_create_product_with_non_numeric_price_raises_validation_error(self):
        payload = {"name": "Toy", "price": "abc", "description": ""}

        with self.assertRaises(ValidationError) as ctx:
            self.service.create_product(payload)

        self.assertIn(
            "Pole 'price' musi być poprawną liczbą.",
            ctx.exception.errors,
        )
        self.mock_repo.create.assert_not_called()
