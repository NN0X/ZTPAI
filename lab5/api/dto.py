from decimal import Decimal, InvalidOperation

class ProductRequestDTO:
    def __init__(self, data: dict):
        self.name = data.get('name')
        self.price = data.get('price')
        self.description = data.get('description', '')

    def validate(self):
        errors = []

        if not self.name or not str(self.name).strip():
            errors.append("Pole 'name' nie może być puste.")

        try:
            price_val = Decimal(str(self.price))
            if price_val < 0:
                errors.append("Pole 'price' musi być >= 0.")
        except (InvalidOperation, TypeError, ValueError):
            errors.append("Pole 'price' musi być poprawną liczbą.")

        return errors


class ProductResponseDTO:
    def __init__(self, product):
        self.id = product.id
        self.name = product.name
        self.price = str(product.price)

    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'price': self.price,
        }
