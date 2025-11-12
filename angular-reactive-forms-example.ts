// ============================================
// ANGULAR REACTIVE FORMS - SUBSCRIBE FORM CONTROL
// ============================================

import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, FormControl, Validators, AbstractControl } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil, debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-example',
  template: `
    <form [formGroup]="myForm" (ngSubmit)="onSubmit()">
      <!-- Form Control với subscribe -->
      <div>
        <label>Email:</label>
        <input type="email" formControlName="email" />
        <div *ngIf="emailControl?.hasError('required') && emailControl?.touched">
          Email is required
        </div>
        <div *ngIf="emailControl?.hasError('email') && emailControl?.touched">
          Invalid email format
        </div>
        <div>Current value: {{ currentEmailValue }}</div>
      </div>

      <!-- Form Control khác -->
      <div>
        <label>Name:</label>
        <input type="text" formControlName="name" />
      </div>

      <!-- Nested Form Group -->
      <div formGroupName="address">
        <label>Street:</label>
        <input type="text" formControlName="street" />
        
        <label>City:</label>
        <input type="text" formControlName="city" />
      </div>

      <button type="submit" [disabled]="myForm.invalid">Submit</button>
    </form>
  `
})
export class ExampleComponent implements OnInit, OnDestroy {
  myForm: FormGroup;
  emailControl: AbstractControl | null = null;
  currentEmailValue: string = '';
  
  private destroy$ = new Subject<void>();

  constructor(private fb: FormBuilder) {
    // Cách 1: Tạo FormGroup với FormBuilder
    this.myForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      name: ['', Validators.required],
      address: this.fb.group({
        street: [''],
        city: ['']
      })
    });

    // Hoặc Cách 2: Tạo FormGroup thủ công
    // this.myForm = new FormGroup({
    //   email: new FormControl('', [Validators.required, Validators.email]),
    //   name: new FormControl('', Validators.required),
    //   address: new FormGroup({
    //     street: new FormControl(''),
    //     city: new FormControl('')
    //   })
    // });
  }

  ngOnInit() {
    // Lấy FormControl từ FormGroup
    this.emailControl = this.myForm.get('email');

    // ============================================
    // SUBSCRIBE FORM CONTROL - CÁC CÁCH SỬ DỤNG
    // ============================================

    // 1. Subscribe FormControl để lắng nghe thay đổi giá trị
    if (this.emailControl) {
      this.emailControl.valueChanges
        .pipe(
          takeUntil(this.destroy$), // Tự động unsubscribe khi component destroy
          debounceTime(300), // Đợi 300ms sau khi user ngừng gõ
          distinctUntilChanged() // Chỉ emit khi giá trị thực sự thay đổi
        )
        .subscribe(value => {
          console.log('Email value changed:', value);
          this.currentEmailValue = value;
          
          // Có thể thực hiện các hành động khác
          // Ví dụ: validate, call API, update UI, etc.
          this.onEmailChange(value);
        });
    }

    // 2. Subscribe FormControl để lắng nghe thay đổi trạng thái (valid/invalid, touched/untouched, etc.)
    if (this.emailControl) {
      this.emailControl.statusChanges
        .pipe(takeUntil(this.destroy$))
        .subscribe(status => {
          console.log('Email status changed:', status); // 'VALID', 'INVALID', 'PENDING', 'DISABLED'
          
          if (status === 'VALID') {
            console.log('Email is valid!');
          } else if (status === 'INVALID') {
            console.log('Email is invalid!');
          }
        });
    }

    // 3. Subscribe toàn bộ FormGroup để lắng nghe thay đổi
    this.myForm.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(formValue => {
        console.log('Form value changed:', formValue);
      });

    // 4. Subscribe trạng thái của FormGroup
    this.myForm.statusChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(status => {
        console.log('Form status:', status);
      });

    // 5. Subscribe nested FormControl (address.street)
    const streetControl = this.myForm.get('address.street');
    if (streetControl) {
      streetControl.valueChanges
        .pipe(takeUntil(this.destroy$))
        .subscribe(value => {
          console.log('Street changed:', value);
        });
    }

    // 6. Subscribe với điều kiện
    if (this.emailControl) {
      this.emailControl.valueChanges
        .pipe(
          takeUntil(this.destroy$),
          distinctUntilChanged()
        )
        .subscribe(value => {
          // Chỉ xử lý khi email hợp lệ
          if (this.emailControl?.valid && value) {
            this.checkEmailAvailability(value);
          }
        });
    }
  }

  // Hàm xử lý khi email thay đổi
  private onEmailChange(email: string) {
    console.log('Processing email change:', email);
    // Có thể thực hiện: validate, call API, update UI, etc.
  }

  // Ví dụ: Kiểm tra email có tồn tại không
  private checkEmailAvailability(email: string) {
    console.log('Checking email availability:', email);
    // Call API to check email
    // this.userService.checkEmail(email).subscribe(...)
  }

  // Submit form
  onSubmit() {
    if (this.myForm.valid) {
      console.log('Form submitted:', this.myForm.value);
      // Xử lý submit
    } else {
      console.log('Form is invalid');
      // Mark all fields as touched để hiển thị lỗi
      this.markFormGroupTouched(this.myForm);
    }
  }

  // Helper: Mark all fields as touched
  private markFormGroupTouched(formGroup: FormGroup) {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();

      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  // Cleanup: Unsubscribe khi component destroy
  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

// ============================================
// VÍ DỤ NÂNG CAO: CUSTOM VALIDATOR VÀ SUBSCRIBE
// ============================================

@Component({
  selector: 'app-advanced-example',
  template: `
    <form [formGroup]="form">
      <input formControlName="username" />
      <div *ngIf="form.get('username')?.hasError('usernameTaken')">
        Username already exists
      </div>
    </form>
  `
})
export class AdvancedExampleComponent implements OnInit, OnDestroy {
  form: FormGroup;
  private destroy$ = new Subject<void>();

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      username: ['', [Validators.required], [this.asyncUsernameValidator.bind(this)]]
    });
  }

  ngOnInit() {
    const usernameControl = this.form.get('username');
    
    if (usernameControl) {
      // Subscribe để xử lý khi username thay đổi
      usernameControl.valueChanges
        .pipe(
          takeUntil(this.destroy$),
          debounceTime(500),
          distinctUntilChanged()
        )
        .subscribe(value => {
          if (value && usernameControl.valid) {
            // Trigger async validation
            usernameControl.updateValueAndValidity();
          }
        });
    }
  }

  // Async Validator
  asyncUsernameValidator(control: AbstractControl): Promise<{ [key: string]: any } | null> {
    return new Promise((resolve) => {
      setTimeout(() => {
        // Simulate API call
        const takenUsernames = ['admin', 'user', 'test'];
        if (takenUsernames.includes(control.value)) {
          resolve({ usernameTaken: true });
        } else {
          resolve(null);
        }
      }, 1000);
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

// ============================================
// VÍ DỤ: SUBSCRIBE VỚI RXJS OPERATORS
// ============================================

@Component({
  selector: 'app-rxjs-example',
  template: `
    <form [formGroup]="searchForm">
      <input formControlName="search" placeholder="Search..." />
      <div>Results: {{ searchResults }}</div>
    </form>
  `
})
export class RxjsExampleComponent implements OnInit, OnDestroy {
  searchForm: FormGroup;
  searchResults: string[] = [];
  private destroy$ = new Subject<void>();

  constructor(private fb: FormBuilder) {
    this.searchForm = this.fb.group({
      search: ['']
    });
  }

  ngOnInit() {
    const searchControl = this.searchForm.get('search');
    
    if (searchControl) {
      searchControl.valueChanges
        .pipe(
          takeUntil(this.destroy$),
          debounceTime(300), // Đợi 300ms sau khi user ngừng gõ
          distinctUntilChanged(), // Chỉ emit khi giá trị thay đổi
          // filter(value => value.length >= 3), // Chỉ search khi có ít nhất 3 ký tự
          // switchMap(value => this.searchService.search(value)) // Cancel previous request
        )
        .subscribe(value => {
          console.log('Searching for:', value);
          // this.performSearch(value);
        });
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

// ============================================
// VÍ DỤ: SET VALUE KHÔNG TRIGGER valueChanges
// ============================================

@Component({
  selector: 'app-set-value-example',
  template: `
    <form [formGroup]="form">
      <input formControlName="email" />
      <input formControlName="name" />
      <button (click)="loadData()">Load Data</button>
      <button (click)="resetForm()">Reset</button>
    </form>
  `
})
export class SetValueExampleComponent implements OnInit, OnDestroy {
  form: FormGroup;
  private destroy$ = new Subject<void>();

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      name: ['', Validators.required]
    });
  }

  ngOnInit() {
    // Subscribe valueChanges
    this.form.valueChanges
      .pipe(
        takeUntil(this.destroy$),
        debounceTime(500)
      )
      .subscribe(formValue => {
        console.log('Form value changed by user:', formValue);
        // Chỉ xử lý khi user thay đổi form
        this.autoSave(formValue);
      });
  }

  // Load data từ API - KHÔNG trigger valueChanges
  loadData() {
    // Simulate API call
    const apiData = {
      email: 'user@example.com',
      name: 'John Doe'
    };

    // ❌ BAD - Sẽ trigger valueChanges (không mong muốn)
    // this.form.patchValue(apiData);

    // ✅ GOOD - Không trigger valueChanges
    this.form.patchValue(apiData, { emitEvent: false });
    console.log('Data loaded from API, valueChanges NOT triggered');
  }

  // Reset form - KHÔNG trigger valueChanges
  resetForm() {
    // ❌ BAD - Sẽ trigger valueChanges
    // this.form.reset();

    // ✅ GOOD - Không trigger valueChanges
    this.form.reset({
      email: '',
      name: ''
    }, { emitEvent: false });
    console.log('Form reset, valueChanges NOT triggered');
  }

  // Set value cho từng control - KHÔNG trigger valueChanges
  setEmailValue(email: string) {
    const emailControl = this.form.get('email');
    
    // ❌ BAD - Sẽ trigger valueChanges
    // emailControl?.setValue(email);

    // ✅ GOOD - Không trigger valueChanges
    emailControl?.setValue(email, { emitEvent: false });
  }

  // Update validation - KHÔNG trigger valueChanges
  updateValidation() {
    const emailControl = this.form.get('email');
    
    // ❌ BAD - Sẽ trigger valueChanges và statusChanges
    // emailControl?.updateValueAndValidity();

    // ✅ GOOD - Không trigger valueChanges, chỉ update validation
    emailControl?.updateValueAndValidity({ emitEvent: false });
  }

  // Auto-save chỉ khi user thay đổi
  private autoSave(formValue: any) {
    console.log('Auto-saving form...', formValue);
    // this.apiService.saveForm(formValue).subscribe();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

// ============================================
// VÍ DỤ: LOAD DATA TỪ API KHÔNG TRIGGER valueChanges
// ============================================

@Component({
  selector: 'app-load-data-example',
  template: `
    <form [formGroup]="form">
      <input formControlName="email" />
      <input formControlName="name" />
      <div>Auto-save count: {{ autoSaveCount }}</div>
    </form>
  `
})
export class LoadDataExampleComponent implements OnInit, OnDestroy {
  form: FormGroup;
  autoSaveCount = 0;
  private destroy$ = new Subject<void>();

  constructor(private fb: FormBuilder, private apiService: any) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      name: ['', Validators.required]
    });
  }

  ngOnInit() {
    // Subscribe valueChanges - chỉ xử lý khi user thay đổi
    this.form.valueChanges
      .pipe(
        takeUntil(this.destroy$),
        debounceTime(500),
        distinctUntilChanged()
      )
      .subscribe(formValue => {
        console.log('User changed form, auto-saving...');
        this.autoSaveCount++;
        this.autoSave(formValue);
      });

    // Load data từ API (không trigger valueChanges)
    this.loadFormData();
  }

  loadFormData() {
    // Simulate API call
    this.apiService.getFormData().subscribe((data: any) => {
      console.log('Loading data from API...', data);
      
      // ✅ GOOD - Set value từ API mà không trigger valueChanges
      this.form.patchValue(data, { emitEvent: false });
      
      console.log('Data loaded, autoSaveCount should still be 0');
      // autoSaveCount vẫn là 0 vì valueChanges không được trigger
    });
  }

  private autoSave(formValue: any) {
    // Chỉ được gọi khi user thay đổi form
    // KHÔNG được gọi khi load data từ API
    console.log('Auto-saving:', formValue);
    // this.apiService.saveForm(formValue).subscribe();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

