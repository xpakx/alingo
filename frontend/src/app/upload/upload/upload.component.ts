import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { SoundService } from 'src/app/alphabet/sound.service';

@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.css']
})
export class UploadComponent implements OnInit {
  files?: FileList;
  @ViewChild('fileselection', {static: true}) fileselectionButton?: ElementRef;

  constructor(private soundService: SoundService) { }

  ngOnInit(): void {
  }

  selectFile(event: Event) {
    const element = event.currentTarget as HTMLInputElement;
    let fileList: FileList | null = element.files;
    if(fileList && fileList.length > 0) {
      let firstFile = fileList.item(0);
      this.files = fileList;
    }
  }

  openFileSelection() {
    if(this.fileselectionButton) {
      this.fileselectionButton.nativeElement.click();
    }
  }

  send() {
    if(!this.files) {
      return;
    }
    this.soundService.sendSound(this.files).subscribe({
      next: (response: any) => {},
      error: (error: HttpErrorResponse) => {}
    });
  }
}
