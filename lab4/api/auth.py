import jwt
import functools
import datetime
from django.http import JsonResponse
from django.conf import settings


def _decode_token(request):
    auth = request.headers.get('Authorization', '')
    if not auth.startswith('Bearer '):
        return None, JsonResponse({'error': 'Brak tokenu autoryzacyjnego'}, status=401)
    token = auth.split(' ', 1)[1]
    try:
        payload = jwt.decode(token, settings.JWT_SECRET, algorithms=[settings.JWT_ALGORITHM])
        return payload, None
    except jwt.ExpiredSignatureError:
        return None, JsonResponse({'error': 'Token wygasł'}, status=401)
    except jwt.InvalidTokenError:
        return None, JsonResponse({'error': 'Nieprawidłowy token'}, status=401)


def generate_token(user):
    payload = {
        'user_id': user.id,
        'username': user.username,
        'role': 'ADMIN' if user.is_staff else 'USER',
        'exp': datetime.datetime.utcnow() + datetime.timedelta(hours=settings.JWT_EXP_HOURS),
    }
    return jwt.encode(payload, settings.JWT_SECRET, algorithm=settings.JWT_ALGORITHM)


def jwt_required(view_func):
    @functools.wraps(view_func)
    def wrapper(request, *args, **kwargs):
        payload, err = _decode_token(request)
        if err:
            return err
        request.jwt_payload = payload
        return view_func(request, *args, **kwargs)
    return wrapper


def admin_required(view_func):
    @functools.wraps(view_func)
    def wrapper(request, *args, **kwargs):
        payload, err = _decode_token(request)
        if err:
            return err
        if payload.get('role') != 'ADMIN':
            return JsonResponse({'error': 'Brak uprawnień – wymagana rola ADMIN'}, status=403)
        request.jwt_payload = payload
        return view_func(request, *args, **kwargs)
    return wrapper
