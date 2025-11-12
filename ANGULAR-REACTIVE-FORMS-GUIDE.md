# Angular Reactive Forms - Subscribe Form Control Guide

## Tổng quan

Angular Reactive Forms sử dụng `FormControl`, `FormGroup`, và `FormArray` để quản lý form. Bạn có thể **subscribe** vào các FormControl để lắng nghe thay đổi giá trị và trạng thái.

---

## 1. Setup cơ bản

### Import cần thiết

```typescript
import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, FormControl, Validators } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil, debounceTime, distinctUntilChanged } from 'rxjs/operators';
```

### Tạo FormGroup

```typescript
export class MyComponent implements OnInit, OnDestroy {
  myForm: FormGroup;
  private destroy$ = new Subject<void>();

  constructor(private fb: FormBuilder) {
    // Cách 1: Sử dụng FormBuilder (Recommended)
    this.myForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      name: ['', Validators.required]
    });

    // Cách 2: Tạo thủ công
    // this.myForm = new FormGroup({
    //   email: new FormControl('', [Validators.required, Validators.email]),
    //   name: new FormControl('', Validators.required)
    // });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
```

---

## 2. Subscribe FormControl - Các cách sử dụng

### 2.1. Subscribe `valueChanges` - Lắng nghe thay đổi giá trị

```typescript
ngOnInit() {
  const emailControl = this.myForm.get('email');
  
  if (emailControl) {
    emailControl.valueChanges
      .pipe(
        takeUntil(this.destroy$), // Tự động unsubscribe khi component destroy
        debounceTime(300), // Đợi 300ms sau khi user ngừng gõ
        distinctUntilChanged() // Chỉ emit khi giá trị thực sự thay đổi
      )
      .subscribe(value => {
        console.log('Email changed:', value);
        // Xử lý logic khi email thay đổi
      });
  }
}
```

**Khi nào sử dụng:**
- Validate real-time
- Auto-save
- Search as you type
- Update UI dựa trên giá trị input

---

### 2.2. Subscribe `statusChanges` - Lắng nghe thay đổi trạng thái

```typescript
ngOnInit() {
  const emailControl = this.myForm.get('email');
  
  if (emailControl) {
    emailControl.statusChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(status => {
        console.log('Email status:', status); // 'VALID', 'INVALID', 'PENDING', 'DISABLED'
        
        if (status === 'VALID') {
          console.log('Email is valid!');
        } else if (status === 'INVALID') {
          console.log('Email is invalid!');
        } else if (status === 'PENDING') {
          console.log('Email validation is pending...');
        }
      });
  }
}
```

**Khi nào sử dụng:**
- Hiển thị loading khi async validation đang chạy
- Enable/disable button dựa trên trạng thái form
- Hiển thị thông báo validation

---

### 2.3. Subscribe FormGroup `valueChanges` - Lắng nghe thay đổi toàn bộ form

```typescript
ngOnInit() {
  this.myForm.valueChanges
    .pipe(takeUntil(this.destroy$))
    .subscribe(formValue => {
      console.log('Form value changed:', formValue);
      // {
      //   email: 'user@example.com',
      //   name: 'John Doe'
      // }
    });
}
```

**Khi nào sử dụng:**
- Auto-save toàn bộ form
- Track form changes
- Undo/Redo functionality

---

### 2.4. Subscribe FormGroup `statusChanges` - Lắng nghe trạng thái form

```typescript
ngOnInit() {
  this.myForm.statusChanges
    .pipe(takeUntil(this.destroy$))
    .subscribe(status => {
      console.log('Form status:', status);
      
      if (status === 'VALID') {
        // Enable submit button
      } else {
        // Disable submit button
      }
    });
}
```

---

## 3. RxJS Operators thường dùng

### 3.1. `debounceTime` - Trì hoãn emit

```typescript
emailControl.valueChanges
  .pipe(
    debounceTime(300) // Đợi 300ms sau khi user ngừng gõ
  )
  .subscribe(value => {
    // Chỉ xử lý sau khi user ngừng gõ 300ms
    this.search(value);
  });
```

**Use case:** Search as you type, API calls

---

### 3.2. `distinctUntilChanged` - Chỉ emit khi giá trị thay đổi

```typescript
emailControl.valueChanges
  .pipe(
    distinctUntilChanged() // Chỉ emit khi giá trị thực sự thay đổi
  )
  .subscribe(value => {
    // Không emit nếu giá trị giống với giá trị trước đó
  });
```

**Use case:** Tránh duplicate API calls

---

### 3.3. `filter` - Lọc giá trị

```typescript
emailControl.valueChanges
  .pipe(
    filter(value => value && value.length >= 3) // Chỉ emit khi có ít nhất 3 ký tự
  )
  .subscribe(value => {
    this.search(value);
  });
```

**Use case:** Chỉ search khi có đủ ký tự

---

### 3.4. `switchMap` - Cancel previous request

```typescript
import { switchMap } from 'rxjs/operators';

emailControl.valueChanges
  .pipe(
    debounceTime(300),
    distinctUntilChanged(),
    switchMap(value => this.apiService.search(value)) // Cancel previous request nếu có request mới
  )
  .subscribe(results => {
    this.searchResults = results;
  });
```

**Use case:** API calls, tránh race condition

---

### 3.5. `takeUntil` - Tự động unsubscribe

```typescript
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

export class MyComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  ngOnInit() {
    this.emailControl.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(value => {
        // ...
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
```

**Use case:** Tránh memory leak, tự động cleanup

---

## 4. Ví dụ thực tế

### 4.1. Search với debounce

```typescript
ngOnInit() {
  const searchControl = this.myForm.get('search');
  
  if (searchControl) {
    searchControl.valueChanges
      .pipe(
        takeUntil(this.destroy$),
        debounceTime(300),
        distinctUntilChanged(),
        filter(value => value && value.length >= 3),
        switchMap(value => this.searchService.search(value))
      )
      .subscribe(results => {
        this.searchResults = results;
      });
  }
}
```

---

### 4.2. Auto-save form

```typescript
ngOnInit() {
  this.myForm.valueChanges
    .pipe(
      takeUntil(this.destroy$),
      debounceTime(1000), // Đợi 1 giây sau khi user ngừng thay đổi
      distinctUntilChanged()
    )
    .subscribe(formValue => {
      this.saveForm(formValue);
    });
}

private saveForm(formValue: any) {
  this.apiService.saveForm(formValue).subscribe(
    () => console.log('Form saved!'),
    error => console.error('Save failed:', error)
  );
}
```

---

### 4.3. Conditional validation

```typescript
ngOnInit() {
  const emailControl = this.myForm.get('email');
  const confirmEmailControl = this.myForm.get('confirmEmail');
  
  if (emailControl && confirmEmailControl) {
    emailControl.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        // Re-validate confirmEmail khi email thay đổi
        confirmEmailControl.updateValueAndValidity();
      });
  }
}
```

---

### 4.4. Enable/disable control dựa trên control khác

```typescript
ngOnInit() {
  const subscribeNewsletterControl = this.myForm.get('subscribeNewsletter');
  const emailControl = this.myForm.get('email');
  
  if (subscribeNewsletterControl && emailControl) {
    subscribeNewsletterControl.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(subscribed => {
        if (subscribed) {
          emailControl.enable();
          emailControl.setValidators([Validators.required, Validators.email]);
        } else {
          emailControl.disable();
          emailControl.clearValidators();
        }
        emailControl.updateValueAndValidity();
      });
  }
}
```

---

## 5. Best Practices

### ✅ DO

1. **Luôn sử dụng `takeUntil` để unsubscribe:**
   ```typescript
   .pipe(takeUntil(this.destroy$))
   ```

2. **Sử dụng `debounceTime` cho API calls:**
   ```typescript
   .pipe(debounceTime(300))
   ```

3. **Sử dụng `distinctUntilChanged` để tránh duplicate:**
   ```typescript
   .pipe(distinctUntilChanged())
   ```

4. **Kiểm tra null trước khi subscribe:**
   ```typescript
   const control = this.myForm.get('email');
   if (control) {
     control.valueChanges.subscribe(...);
   }
   ```

5. **Sử dụng `switchMap` cho API calls:**
   ```typescript
   .pipe(switchMap(value => this.apiService.search(value)))
   ```

---

### ❌ DON'T

1. **Không quên unsubscribe:**
   ```typescript
   // ❌ BAD - Memory leak
   this.emailControl.valueChanges.subscribe(...);
   
   // ✅ GOOD
   this.emailControl.valueChanges
     .pipe(takeUntil(this.destroy$))
     .subscribe(...);
   ```

2. **Không subscribe quá nhiều lần:**
   ```typescript
   // ❌ BAD - Multiple subscriptions
   ngOnInit() {
     this.emailControl.valueChanges.subscribe(...);
     this.emailControl.valueChanges.subscribe(...);
   }
   
   // ✅ GOOD - Single subscription
   ngOnInit() {
     this.emailControl.valueChanges
       .pipe(takeUntil(this.destroy$))
       .subscribe(...);
   }
   ```

3. **Không gọi API quá nhiều:**
   ```typescript
   // ❌ BAD - API call mỗi lần value thay đổi
   this.emailControl.valueChanges.subscribe(value => {
     this.apiService.search(value).subscribe(...);
   });
   
   // ✅ GOOD - Debounce và switchMap
   this.emailControl.valueChanges
     .pipe(
       debounceTime(300),
       switchMap(value => this.apiService.search(value))
     )
     .subscribe(...);
   ```

---

## 6. Set Value Không Trigger valueChanges

Khi bạn muốn set value vào FormControl nhưng **không muốn trigger** `valueChanges`, sử dụng option `emitEvent: false`.

### 6.1. FormControl.setValue() với emitEvent: false

```typescript
const emailControl = this.myForm.get('email');

// ❌ BAD - Sẽ trigger valueChanges
emailControl?.setValue('user@example.com');

// ✅ GOOD - Không trigger valueChanges
emailControl?.setValue('user@example.com', { emitEvent: false });
```

**Ví dụ sử dụng:**

```typescript
ngOnInit() {
  const emailControl = this.myForm.get('email');
  
  // Subscribe valueChanges
  emailControl?.valueChanges
    .pipe(takeUntil(this.destroy$))
    .subscribe(value => {
      console.log('Email changed:', value);
      // Logic xử lý khi user thay đổi
    });
  
  // Load data từ API và set value (không trigger valueChanges)
  this.loadUserData();
}

loadUserData() {
  this.userService.getCurrentUser().subscribe(user => {
    // Set value từ API mà không trigger valueChanges
    this.myForm.patchValue({
      email: user.email,
      name: user.name
    }, { emitEvent: false }); // Không trigger valueChanges
    
    // Hoặc set từng control riêng lẻ
    // this.myForm.get('email')?.setValue(user.email, { emitEvent: false });
    // this.myForm.get('name')?.setValue(user.name, { emitEvent: false });
  });
}
```

---

### 6.2. FormGroup.patchValue() với emitEvent: false

```typescript
// ❌ BAD - Sẽ trigger valueChanges cho tất cả controls
this.myForm.patchValue({
  email: 'user@example.com',
  name: 'John Doe'
});

// ✅ GOOD - Không trigger valueChanges
this.myForm.patchValue({
  email: 'user@example.com',
  name: 'John Doe'
}, { emitEvent: false });
```

---

### 6.3. FormGroup.setValue() với emitEvent: false

```typescript
// ❌ BAD - Sẽ trigger valueChanges
this.myForm.setValue({
  email: 'user@example.com',
  name: 'John Doe'
});

// ✅ GOOD - Không trigger valueChanges
this.myForm.setValue({
  email: 'user@example.com',
  name: 'John Doe'
}, { emitEvent: false });
```

**Lưu ý:** `setValue()` yêu cầu **tất cả** controls phải có value, còn `patchValue()` chỉ set những controls được cung cấp.

---

### 6.4. updateValueAndValidity() với emitEvent: false

```typescript
const emailControl = this.myForm.get('email');

// ❌ BAD - Sẽ trigger valueChanges và statusChanges
emailControl?.updateValueAndValidity();

// ✅ GOOD - Không trigger valueChanges, chỉ update validation
emailControl?.updateValueAndValidity({ emitEvent: false });
```

---

### 6.5. Ví dụ thực tế: Reset form không trigger valueChanges

```typescript
resetForm() {
  // Reset form về giá trị mặc định mà không trigger valueChanges
  this.myForm.reset({
    email: '',
    name: ''
  }, { emitEvent: false });
  
  // Hoặc reset về giá trị ban đầu
  this.myForm.reset(this.initialFormValue, { emitEvent: false });
}
```

---

### 6.6. Ví dụ thực tế: Load data từ API

```typescript
ngOnInit() {
  // Subscribe valueChanges
  this.myForm.valueChanges
    .pipe(
      takeUntil(this.destroy$),
      debounceTime(500)
    )
    .subscribe(formValue => {
      // Chỉ xử lý khi user thay đổi form
      this.autoSave(formValue);
    });
  
  // Load data từ API (không trigger valueChanges)
  this.loadFormData();
}

loadFormData() {
  this.apiService.getFormData().subscribe(data => {
    // Set value từ API mà không trigger valueChanges
    this.myForm.patchValue(data, { emitEvent: false });
  });
}

autoSave(formValue: any) {
  // Chỉ được gọi khi user thay đổi form
  // Không được gọi khi load data từ API
  this.apiService.saveForm(formValue).subscribe();
}
```

---

### 6.7. Ví dụ thực tế: Conditional set value

```typescript
ngOnInit() {
  const countryControl = this.myForm.get('country');
  const cityControl = this.myForm.get('city');
  
  countryControl?.valueChanges
    .pipe(takeUntil(this.destroy$))
    .subscribe(country => {
      // Khi country thay đổi, reset city (không trigger valueChanges)
      cityControl?.setValue('', { emitEvent: false });
      
      // Load cities mới
      this.loadCities(country);
    });
}

loadCities(country: string) {
  this.apiService.getCities(country).subscribe(cities => {
    // Set cities vào dropdown mà không trigger valueChanges
    this.cities = cities;
  });
}
```

---

### 6.8. So sánh các options

| Method | emitEvent | Trigger valueChanges? | Trigger statusChanges? | Use Case |
|--------|-----------|----------------------|----------------------|----------|
| `setValue(value)` | `true` (default) | ✅ Yes | ✅ Yes | User input |
| `setValue(value, { emitEvent: false })` | `false` | ❌ No | ❌ No | Load data từ API |
| `patchValue(value)` | `true` (default) | ✅ Yes | ✅ Yes | User input |
| `patchValue(value, { emitEvent: false })` | `false` | ❌ No | ❌ No | Load data từ API |
| `reset(value)` | `true` (default) | ✅ Yes | ✅ Yes | User reset |
| `reset(value, { emitEvent: false })` | `false` | ❌ No | ❌ No | Programmatic reset |
| `updateValueAndValidity()` | `true` (default) | ✅ Yes | ✅ Yes | Re-validate |
| `updateValueAndValidity({ emitEvent: false })` | `false` | ❌ No | ❌ No | Re-validate silently |

---

### 6.9. Best Practices

#### ✅ DO

1. **Sử dụng `emitEvent: false` khi load data từ API:**
   ```typescript
   this.myForm.patchValue(apiData, { emitEvent: false });
   ```

2. **Sử dụng `emitEvent: false` khi reset form programmatically:**
   ```typescript
   this.myForm.reset(defaultValues, { emitEvent: false });
   ```

3. **Sử dụng `emitEvent: false` khi set value từ control khác:**
   ```typescript
   emailControl?.setValue(calculatedValue, { emitEvent: false });
   ```

#### ❌ DON'T

1. **Không dùng `emitEvent: false` khi user input:**
   ```typescript
   // ❌ BAD - User input nên trigger valueChanges
   emailControl?.setValue(userInput, { emitEvent: false });
   
   // ✅ GOOD - User input nên trigger valueChanges
   // (Không cần set programmatically, user đã input trực tiếp)
   ```

2. **Không quên `emitEvent: false` khi load data:**
   ```typescript
   // ❌ BAD - Load data sẽ trigger valueChanges không mong muốn
   this.myForm.patchValue(apiData);
   
   // ✅ GOOD - Load data không trigger valueChanges
   this.myForm.patchValue(apiData, { emitEvent: false });
   ```

---

## 7. Template sử dụng

```html
<form [formGroup]="myForm" (ngSubmit)="onSubmit()">
  <!-- Form Control -->
  <div>
    <label>Email:</label>
    <input type="email" formControlName="email" />
    
    <!-- Hiển thị lỗi -->
    <div *ngIf="myForm.get('email')?.hasError('required') && myForm.get('email')?.touched">
      Email is required
    </div>
    <div *ngIf="myForm.get('email')?.hasError('email') && myForm.get('email')?.touched">
      Invalid email format
    </div>
  </div>

  <!-- Submit button -->
  <button type="submit" [disabled]="myForm.invalid">Submit</button>
</form>
```

---

## 8. Tóm tắt

| Use Case | Operator | Ví dụ |
|----------|----------|-------|
| Lắng nghe thay đổi giá trị | `valueChanges` | `control.valueChanges.subscribe(...)` |
| Lắng nghe thay đổi trạng thái | `statusChanges` | `control.statusChanges.subscribe(...)` |
| Trì hoãn emit | `debounceTime(300)` | Search as you type |
| Chỉ emit khi thay đổi | `distinctUntilChanged()` | Tránh duplicate |
| Lọc giá trị | `filter(...)` | Chỉ xử lý khi đủ điều kiện |
| Cancel previous request | `switchMap(...)` | API calls |
| Tự động unsubscribe | `takeUntil(this.destroy$)` | Tránh memory leak |

---

## 9. Tài liệu tham khảo

- [Angular Reactive Forms](https://angular.io/guide/reactive-forms)
- [RxJS Operators](https://rxjs.dev/guide/operators)
- [FormControl API](https://angular.io/api/forms/FormControl)

