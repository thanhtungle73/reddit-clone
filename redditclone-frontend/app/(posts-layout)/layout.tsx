import React, { ReactNode } from 'react';

export interface PostLayoutProps {
  children: ReactNode;
}

export default function PostLayout({ children }: PostLayoutProps) {
  return (
    <>
      <main className='mt-12'>{children}</main>
    </>
  );
}
