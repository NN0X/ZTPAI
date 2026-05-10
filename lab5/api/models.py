from django.db import models

class Product(models.Model):
    name = models.CharField(max_length=100)
    price = models.DecimalField(max_digits=10, decimal_places=2)
    description = models.TextField(blank=True, null=True)

    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'price': str(self.price),
            'description': self.description
        }
