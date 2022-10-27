import React from 'react';
import './App.css';
import { LocomotiveScrollProvider } from "react-locomotive-scroll";
import { useRef } from "react";

function App() {
    const ref = useRef(null);

    const options = {
        smooth: true,
    }

    return (
        <LocomotiveScrollProvider options={options} containerRef={ref}>
            <main data-scroll-container ref={ref}>
                <section className="intro" data-scroll-section>
                    <h1>This is the Introduction section</h1>
                </section>
                <section className="contents" data-scroll-section>
                    <h1
                        data-scroll
                        data-scroll-direction="horizontal"
                        data-scroll-speed="9"
                    >
                        I Love React
                    </h1>
                    <h1
                        data-scroll
                        data-scroll-direction="vertical"
                        data-scroll-speed="9" // Values provided here affect the animations
                    >
                        That's why I code every day
                    </h1>
                </section>
                <section className="footer" data-scroll-section>
                    <h1>Let's end the application with this Footer</h1>
                </section>
            </main>
        </LocomotiveScrollProvider>
    );
}

export default App;
