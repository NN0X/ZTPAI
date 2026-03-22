from django.shortcuts import render, get_object_or_404
from django.http import HttpResponse, JsonResponse
from django.views.decorators.csrf import csrf_exempt
import json
from .models import Product

def hello_world(request):
    return HttpResponse("Hello, World!")

def hello_name(request, name):
    return HttpResponse(f"Hello, {name}!")

def greet(request):
    name = request.GET.get('name', 'Gość')
    return HttpResponse(f"Hello, {name}!")

def info(request):
    data = {
        "autor": "Tomasz Sarkowicz",
        "framework": "Django",
        "wersja aplikacji": "1.0.0"
    }
    return JsonResponse(data)

@csrf_exempt
def product_list_create(request):
    if request.method == 'GET':
        products = Product.objects.all()
        data = [product.to_dict() for product in products]
        return JsonResponse(data, safe=False)

    elif request.method == 'POST':
        try:
            body = json.loads(request.body)
            product = Product.objects.create(
                name=body.get('name'),
                price=body.get('price'),
                description=body.get('description', '')
            )
            return JsonResponse(product.to_dict(), status=201)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=400)

@csrf_exempt
def product_detail_delete(request, id):
    product = get_object_or_404(Product, id=id)

    if request.method == 'GET':
        return JsonResponse(product.to_dict())
    elif request.method == 'DELETE':
        product.delete()
        return JsonResponse({'message': 'Rekord usunięty pomyślnie'}, status=204)
